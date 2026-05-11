package com.library.dao.impl;

import com.library.db.DBConnection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class FakeJdbc {
    private FakeJdbc() {
    }

    static Db install() {
        Db db = new Db();
        setSingletonConnection(db.connection());
        return db;
    }

    static void uninstall() {
        try {
            Field instance = DBConnection.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot clear DBConnection singleton", e);
        }
    }

    static Map<Object, Object> row(Object... pairs) {
        Map<Object, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            row.put(pairs[i], pairs[i + 1]);
        }
        return row;
    }

    static final class Db {
        private final List<StatementCall> statements = new ArrayList<>();
        private final Connection connection = proxy(Connection.class, new ConnectionHandler(this));
        private List<Map<Object, Object>> rows = List.of();
        private SQLException queryException;
        private SQLException updateException;
        private boolean closed;

        Connection connection() {
            return connection;
        }

        void rows(Map<Object, Object>... rows) {
            this.rows = List.of(rows);
            this.queryException = null;
        }

        void emptyRows() {
            rows();
        }

        void failQuery(SQLException exception) {
            this.queryException = exception;
        }

        void failUpdate(SQLException exception) {
            this.updateException = exception;
        }

        StatementCall lastStatement() {
            if (statements.isEmpty()) {
                throw new AssertionError("No prepared statement was created");
            }
            return statements.get(statements.size() - 1);
        }

        List<StatementCall> statements() {
            return statements;
        }
    }

    static final class StatementCall {
        private final String sql;
        private final List<Param> params = new ArrayList<>();
        private boolean queryExecuted;
        private boolean updateExecuted;

        StatementCall(String sql) {
            this.sql = sql;
        }

        String sql() {
            return sql;
        }

        boolean queryExecuted() {
            return queryExecuted;
        }

        boolean updateExecuted() {
            return updateExecuted;
        }

        long countStringsContaining(String text) {
            return params.stream()
                    .filter(p -> "setString".equals(p.method))
                    .filter(p -> p.value instanceof String s && s.contains(text))
                    .count();
        }

        boolean hasStringValue(String value) {
            return params.stream()
                    .anyMatch(p -> "setString".equals(p.method) && value.equals(p.value));
        }

        boolean hasIntValue(int value) {
            return params.stream()
                    .anyMatch(p -> "setInt".equals(p.method) && Integer.valueOf(value).equals(p.value));
        }

        boolean hasDateValue(Date value) {
            return params.stream()
                    .anyMatch(p -> "setDate".equals(p.method) && value.equals(p.value));
        }

        boolean hasSetNull(int index, int sqlType) {
            return params.stream()
                    .anyMatch(p -> "setNull".equals(p.method)
                            && p.index == index
                            && Integer.valueOf(sqlType).equals(p.value));
        }

        boolean hasSetString(int index, String value) {
            return params.stream()
                    .anyMatch(p -> "setString".equals(p.method)
                            && p.index == index
                            && value.equals(p.value));
        }

        boolean hasSetInt(int index, int value) {
            return params.stream()
                    .anyMatch(p -> "setInt".equals(p.method)
                            && p.index == index
                            && Integer.valueOf(value).equals(p.value));
        }

        boolean hasSetDate(int index, Date value) {
            return params.stream()
                    .anyMatch(p -> "setDate".equals(p.method)
                            && p.index == index
                            && value.equals(p.value));
        }

        long countMethod(String method) {
            return params.stream().filter(p -> method.equals(p.method)).count();
        }

        private void addParam(String method, Object[] args) {
            params.add(new Param(method, (Integer) args[0], args[1]));
        }
    }

    private record Param(String method, int index, Object value) {
    }

    private static final class ConnectionHandler implements InvocationHandler {
        private final Db db;

        private ConnectionHandler(Db db) {
            this.db = db;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "prepareStatement" -> {
                    StatementCall call = new StatementCall((String) args[0]);
                    db.statements.add(call);
                    yield proxy(PreparedStatement.class, new PreparedStatementHandler(db, call));
                }
                case "isClosed" -> db.closed;
                case "close" -> {
                    db.closed = true;
                    yield null;
                }
                case "unwrap" -> null;
                case "isWrapperFor" -> false;
                case "toString" -> "FakeJdbc.Connection";
                default -> defaultValue(method.getReturnType());
            };
        }
    }

    private static final class PreparedStatementHandler implements InvocationHandler {
        private final Db db;
        private final StatementCall call;

        private PreparedStatementHandler(Db db, StatementCall call) {
            this.db = db;
            this.call = call;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "setString", "setInt", "setDate", "setNull" -> {
                    call.addParam(method.getName(), args);
                    yield null;
                }
                case "executeQuery" -> {
                    call.queryExecuted = true;
                    if (db.queryException != null) {
                        throw db.queryException;
                    }
                    yield proxy(ResultSet.class, new ResultSetHandler(db.rows));
                }
                case "executeUpdate" -> {
                    call.updateExecuted = true;
                    if (db.updateException != null) {
                        throw db.updateException;
                    }
                    yield 1;
                }
                case "close" -> null;
                case "unwrap" -> null;
                case "isWrapperFor" -> false;
                case "toString" -> "FakeJdbc.PreparedStatement";
                default -> defaultValue(method.getReturnType());
            };
        }
    }

    private static final class ResultSetHandler implements InvocationHandler {
        private final List<Map<Object, Object>> rows;
        private int index = -1;

        private ResultSetHandler(List<Map<Object, Object>> rows) {
            this.rows = rows;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "next" -> ++index < rows.size();
                case "getInt" -> {
                    Object value = current().get(args[0]);
                    yield value == null ? 0 : ((Number) value).intValue();
                }
                case "getString" -> {
                    Object value = current().get(args[0]);
                    yield value == null ? null : value.toString();
                }
                case "getDate" -> (Date) current().get(args[0]);
                case "close" -> null;
                case "unwrap" -> null;
                case "isWrapperFor" -> false;
                case "toString" -> "FakeJdbc.ResultSet";
                default -> defaultValue(method.getReturnType());
            };
        }

        private Map<Object, Object> current() {
            if (index < 0 || index >= rows.size()) {
                throw new IllegalStateException("ResultSet is not positioned on a row");
            }
            return rows.get(index);
        }
    }

    private static void setSingletonConnection(Connection connection) {
        try {
            DBConnection dbConnection = allocateDbConnection();

            Field connectionField = DBConnection.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(dbConnection, connection);

            Field instanceField = DBConnection.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, dbConnection);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot install fake DBConnection", e);
        }
    }

    private static DBConnection allocateDbConnection() throws ReflectiveOperationException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field field = unsafeClass.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Object unsafe = field.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (DBConnection) allocateInstance.invoke(unsafe, DBConnection.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }
}
