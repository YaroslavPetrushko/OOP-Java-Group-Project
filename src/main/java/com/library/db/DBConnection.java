package com.library.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton wrapper around the JDBC {@link Connection} to the PostgreSQL database.
 *
 * <p>Connection parameters are read at startup from a {@code .env} file in the
 * project root via the {@code dotenv-java} library. The expected keys are:
 * <ul>
 *   <li>{@code DB_URL}      — full JDBC URL, e.g. {@code jdbc:postgresql://localhost:5432/library}</li>
 *   <li>{@code DB_USER}     — database username</li>
 *   <li>{@code DB_PASSWORD} — database password</li>
 * </ul>
 *
 * <p>The {@link #getInstance()} method re-creates the connection automatically
 * if the existing one has been closed (e.g. after a network interruption).
 *
 * <p><strong>Note:</strong> this implementation uses a single shared
 * {@link Connection}, which is suitable for a desktop application with one
 * active user. For multi-user or server scenarios, a connection pool
 * (e.g. HikariCP) should be used instead.
 */
public class DBConnection {

    /** The single shared instance; {@code null} until first use. */
    private static DBConnection instance;

    /** The underlying JDBC connection; may be {@code null} if connection failed. */
    private Connection connection;

    /**
     * Private constructor — loads credentials from {@code .env} and opens
     * the JDBC connection. Called only by {@link #getInstance()}.
     */
    private DBConnection() {
        Dotenv dotenv = Dotenv.load();

        String url      = dotenv.get("DB_URL");
        String user     = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        try {
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("  ✅ Successful connection to database!");
        } catch (SQLException e) {
            System.err.println("  ❌ Connection error: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton instance, creating or re-creating it if necessary.
     *
     * <p>If the current connection is {@code null} or has been closed, a new
     * {@link DBConnection} is instantiated, which re-reads {@code .env} and
     * opens a fresh JDBC connection.
     *
     * @return the singleton {@link DBConnection}; never {@code null}
     *         (though the inner {@link Connection} may be {@code null} if
     *         the database is unreachable)
     */
    public static DBConnection getInstance() {
        try {
            if (instance == null
                    || instance.connection == null
                    || instance.connection.isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Returns the raw JDBC {@link Connection}.
     *
     * <p>Callers should <em>not</em> close this connection — it is managed
     * entirely by {@link DBConnection}.
     *
     * @return the shared JDBC connection, or {@code null} if the connection
     *         attempt failed
     */
    public Connection getConnection() {
        return connection;
    }
}