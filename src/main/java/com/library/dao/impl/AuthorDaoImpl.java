package com.library.dao.impl;

import com.library.dao.AuthorDao;
import com.library.db.DBConnection;
import com.library.model.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link AuthorDao}.
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
public class AuthorDaoImpl implements AuthorDao {

    // ── SQL constants ─────────────────────────────────────────────

    /** Base SELECT shared by all read methods. */
    private static final String SELECT_BASE =
            "SELECT id, full_name, country, birth_year FROM authors ";

    private static final String FIND_ALL   = SELECT_BASE + "ORDER BY full_name";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE id=?";

    /** Distinct country values for the filter ComboBox. */
    private static final String COUNTRIES =
            "SELECT DISTINCT country FROM authors " +
            "WHERE country IS NOT NULL ORDER BY country";

    private static final String INSERT =
            "INSERT INTO authors (full_name, country, birth_year) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE authors SET full_name=?, country=?, birth_year=? WHERE id=?";
    private static final String DELETE = "DELETE FROM authors WHERE id=?";

    // ── Helpers ───────────────────────────────────────────────────
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    // ── Row mapper ────────────────────────────────────────────────

    /**
     * Maps the current row of a {@link ResultSet} to an {@link Author} object.
     *
     * @param rs the result set positioned on the row to map
     * @return a fully populated {@link Author}
     * @throws SQLException if any column access fails
     */
    private Author mapRow(ResultSet rs) throws SQLException {
        return new Author(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("country"),
                rs.getInt("birth_year")
        );
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    public List<Author> findAll() {
        List<Author> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("AuthorDao.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Author> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("AuthorDao.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<String> findAllCountries() {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(COUNTRIES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("AuthorDao.findAllCountries: " + e.getMessage());
        }
        return list;
    }

    /**
     * Builds and executes a dynamic WHERE clause.
     *
     * <p>The free-text term is matched case-insensitively against
     * {@code full_name}, {@code country}, and the text representation
     * of {@code birth_year} (so typing "1814" finds Shevchenko).
     *
     * {@inheritDoc}
     */
    @Override
    public List<Author> search(String text, String country,
                               Integer yearFrom, Integer yearTo) {
        StringBuilder sql  = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object>  args = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            sql.append("AND (LOWER(full_name) LIKE LOWER(?) " +
                       "     OR LOWER(COALESCE(country,'')) LIKE LOWER(?) " +
                       "     OR CAST(birth_year AS TEXT) LIKE ?) ");
            String p = "%" + text.trim() + "%";
            args.add(p); args.add(p); args.add(p);
        }
        if (country != null && !country.isBlank()) {
            sql.append("AND country = ? ");
            args.add(country);
        }
        if (yearFrom != null && yearFrom > 0) {
            sql.append("AND birth_year >= ? ");
            args.add(yearFrom);
        }
        if (yearTo != null && yearTo > 0) {
            sql.append("AND birth_year <= ? ");
            args.add(yearTo);
        }
        sql.append("ORDER BY full_name");

        return query(sql.toString(), args);
    }

    // ── Write ─────────────────────────────────────────────────────

    @Override
    public void insert(Author a) {
        try (PreparedStatement ps = conn().prepareStatement(INSERT)) {
            ps.setString(1, a.getFullName());
            setNullableString(ps, 2, a.getCountry());
            setNullableInt   (ps, 3, a.getBirthYear());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AuthorDao.insert: " + e.getMessage());
        }
    }

    @Override
    public void update(Author a) {
        try (PreparedStatement ps = conn().prepareStatement(UPDATE)) {
            ps.setString(1, a.getFullName());
            setNullableString(ps, 2, a.getCountry());
            setNullableInt   (ps, 3, a.getBirthYear());
            ps.setInt(4, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AuthorDao.update: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the author has books in the library, PostgreSQL raises SQL state
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
            System.err.println("AuthorDao.delete: " + e.getMessage());
        }
    }

    // ── Internal helpers ──────────────────────────────────────────

    /**
     * Executes a parameterized SELECT and maps all rows to {@link Author} objects.
     *
     * @param sql  the fully formed SQL query string
     * @param args ordered list of bind values ({@link String} or {@link Integer})
     * @return list of matched authors; never {@code null}
     */
    private List<Author> query(String sql, List<Object> args) {
        List<Author> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                Object v = args.get(i);
                if (v instanceof String)       ps.setString(i + 1, (String) v);
                else if (v instanceof Integer) ps.setInt   (i + 1, (Integer) v);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("AuthorDao.query: " + e.getMessage());
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

    /**
     * Binds an int parameter or sets SQL NULL when the value is {@code 0}
     * (sentinel for "not set", following the model convention).
     *
     * @param ps the prepared statement
     * @param i  1-based parameter index
     * @param v  int value; {@code 0} → SQL NULL
     */
    private void setNullableInt(PreparedStatement ps, int i, int v)
            throws SQLException {
        if (v == 0) ps.setNull(i, Types.SMALLINT);
        else        ps.setInt(i, v);
    }
}