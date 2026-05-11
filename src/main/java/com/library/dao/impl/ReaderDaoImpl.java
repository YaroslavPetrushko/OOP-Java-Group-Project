package com.library.dao.impl;

import com.library.dao.ReaderDao;
import com.library.db.DBConnection;
import com.library.model.Reader;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ReaderDao}.
 *
 * <p>All queries are executed via {@link PreparedStatement} — no string
 * concatenation is used anywhere in this class.
 *
 * <p>The {@link #search} method builds a dynamic WHERE clause at runtime
 * from the provided filter arguments.
 *
 * <p>SQL state {@code 23503} (foreign-key violation) from PostgreSQL is caught
 * in {@link #delete} and re-thrown as a descriptive {@link RuntimeException}
 * so the controller layer can display a user-friendly error dialog.
 */
public class ReaderDaoImpl implements ReaderDao {

    // ── SQL constants ─────────────────────────────────────────────

    /** Base SELECT shared by all read methods. */
    private static final String SELECT_BASE =
            "SELECT id, full_name, email, phone, reg_date FROM readers ";

    private static final String FIND_ALL   = SELECT_BASE + "ORDER BY full_name";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE id=?";

    private static final String INSERT =
            "INSERT INTO readers (full_name, email, phone, reg_date) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE readers SET full_name=?, email=?, phone=?, reg_date=? WHERE id=?";
    private static final String DELETE = "DELETE FROM readers WHERE id=?";

    /**
     * PostgreSQL SQL state code for foreign-key violation.
     * Thrown when trying to delete a reader who still has loans.
     */
    private static final String FK_VIOLATION = "23503";

    // ── Connection helper ─────────────────────────────────────────

    /** @return the active JDBC connection from the singleton pool */
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    // ── Row mapper ────────────────────────────────────────────────

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Reader} object.
     *
     * <p>If {@code reg_date} is NULL in the database (should not happen given
     * the schema default), falls back to {@link LocalDate#now()}.
     *
     * @param rs the result set positioned on the row to map
     * @return a fully populated {@link Reader}
     * @throws SQLException if any column access fails
     */
    private Reader mapRow(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("reg_date");
        return new Reader(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now()
        );
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    public List<Reader> findAll() {
        List<Reader> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("ReaderDao.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Reader> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("ReaderDao.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Builds and executes a dynamic WHERE clause.
     *
     * <p>The text term is matched against {@code full_name}, {@code email},
     * and {@code phone} using {@code COALESCE} to safely handle NULL values
     * in nullable columns.
     *
     * {@inheritDoc}
     */
    @Override
    public List<Reader> search(String text, LocalDate regFrom, LocalDate regTo) {
        StringBuilder    sql  = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object>     args = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            sql.append("AND (LOWER(full_name) LIKE LOWER(?) " +
                       "     OR LOWER(COALESCE(email,'')) LIKE LOWER(?) " +
                       "     OR LOWER(COALESCE(phone,'')) LIKE LOWER(?)) ");
            String p = "%" + text.trim() + "%";
            args.add(p); args.add(p); args.add(p);
        }
        if (regFrom != null) {
            sql.append("AND reg_date >= ? ");
            args.add(regFrom);
        }
        if (regTo != null) {
            sql.append("AND reg_date <= ? ");
            args.add(regTo);
        }
        sql.append("ORDER BY full_name");

        return query(sql.toString(), args);
    }

    // ── Write ─────────────────────────────────────────────────────

    @Override
    public void insert(Reader r) {
        try (PreparedStatement ps = conn().prepareStatement(INSERT)) {
            ps.setString(1, r.getFullName());
            setNullableString(ps, 2, r.getEmail());
            setNullableString(ps, 3, r.getPhone());
            ps.setDate(4, Date.valueOf(r.getRegDate() != null ? r.getRegDate() : LocalDate.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReaderDao.insert: " + e.getMessage());
            throw new RuntimeException("Failed to add reader: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Reader r) {
        try (PreparedStatement ps = conn().prepareStatement(UPDATE)) {
            ps.setString(1, r.getFullName());
            setNullableString(ps, 2, r.getEmail());
            setNullableString(ps, 3, r.getPhone());
            ps.setDate(4, Date.valueOf(r.getRegDate() != null ? r.getRegDate() : LocalDate.now()));
            ps.setInt(5, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReaderDao.update: " + e.getMessage());
            throw new RuntimeException("Failed to update reader: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the reader still has loan records, PostgreSQL raises SQL state
     * {@code 23503}. This is caught and re-thrown as a {@link RuntimeException}
     * with a human-readable message so the controller can show an error dialog
     * instead of silently failing.
     */
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReaderDao.delete: " + e.getMessage());
            if (FK_VIOLATION.equals(e.getSQLState())) {
                throw new RuntimeException(
                        "Cannot delete this reader — they still have loan records.\n" +
                                "Delete or close all related loans first.", e);
            }
            throw new RuntimeException("Failed to delete reader: " + e.getMessage(), e);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────

    /**
     * Executes a parameterized SELECT and maps all rows to {@link Reader} objects.
     *
     * @param sql  the fully formed SQL query string
     * @param args ordered list of bind values ({@link String}, {@link Date},
     *             or {@link LocalDate})
     * @return list of matched readers; never {@code null}
     */
    private List<Reader> query(String sql, List<Object> args) {
        List<Reader> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                Object v = args.get(i);
                if (v instanceof String)       ps.setString(i + 1, (String) v);
                else if (v instanceof Date)    ps.setDate  (i + 1, (Date) v);
                else if (v instanceof LocalDate) ps.setDate(i + 1, Date.valueOf((LocalDate) v));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("ReaderDao.query: " + e.getMessage());
        }
        return list;
    }

    /**
     * Binds a {@link String} parameter or sets SQL NULL when blank.
     *
     * @param ps the prepared statement
     * @param i  1-based parameter index
     * @param v  string value; {@code null} or blank → SQL NULL
     */
    private void setNullableString(PreparedStatement ps, int i, String v)
            throws SQLException {
        if (v == null || v.isBlank()) ps.setNull(i, Types.VARCHAR);
        else                          ps.setString(i, v);
    }
}