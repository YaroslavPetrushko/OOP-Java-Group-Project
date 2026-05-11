package com.library.dao.impl;

import com.library.dao.BookDao;
import com.library.db.DBConnection;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link BookDao}.
 *
 * <p>All queries are executed via {@link PreparedStatement} — no string
 * concatenation is used anywhere in this class.
 *
 * <p>The {@link #search} method builds a dynamic WHERE clause at runtime
 * by appending only the conditions that correspond to non-blank/non-null
 * filter arguments.
 *
 * <p>SQL state {@code 23503} (foreign-key violation) from PostgreSQL is caught
 * in {@link #delete} and re-thrown as a descriptive {@link RuntimeException}
 * so the controller layer can display a user-friendly error dialog.
 */
public class BookDaoImpl implements BookDao {

    // ── SQL constants ─────────────────────────────────────────────

    /** Base SELECT used by all read methods to avoid repetition. */
    private static final String SELECT_BASE =
            "SELECT b.id, b.title, b.author_id, a.full_name AS author_name, " +
            "       b.genre, b.isbn, b.pub_year, b.copies " +
            "FROM books b JOIN authors a ON a.id = b.author_id ";

    private static final String FIND_ALL   = SELECT_BASE + "ORDER BY b.id";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE b.id = ?";

    /** Distinct genres for the filter ComboBox. */
    private static final String GENRES =
            "SELECT DISTINCT genre FROM books " +
            "WHERE genre IS NOT NULL ORDER BY genre";

    private static final String INSERT =
            "INSERT INTO books (title, author_id, genre, isbn, pub_year, copies) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE books " +
            "SET title=?, author_id=?, genre=?, isbn=?, pub_year=?, copies=? " +
            "WHERE id=?";

    private static final String DELETE = "DELETE FROM books WHERE id=?";

    /**
     * PostgreSQL SQL state code for foreign-key violation.
     * Thrown when trying to delete a book that still has loans.
     */
    private static final String FK_VIOLATION = "23503";

    // ── Connection helper ─────────────────────────────────────────

    /** @return the active JDBC connection from the singleton pool */
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    // ── Row mapper ────────────────────────────────────────────────

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Book} object.
     *
     * @param rs the result set positioned on the row to map
     * @return a fully populated {@link Book}
     * @throws SQLException if any column access fails
     */
    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getInt("author_id"),
                rs.getString("author_name"),
                rs.getString("genre"),
                rs.getString("isbn"),
                rs.getInt("pub_year"),
                rs.getInt("copies")
        );
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BookDao.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Book> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("BookDao.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<String> findAllGenres() {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(GENRES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("BookDao.findAllGenres: " + e.getMessage());
        }
        return list;
    }

    /**
     * Builds and executes a dynamic WHERE clause.
     *
     * <p>Only non-blank / non-null arguments contribute a condition.
     * The year bounds use {@code > 0} as the sentinel for "not provided"
     * (consistent with the {@code 0 = not set} convention used in the model).
     *
     * {@inheritDoc}
     */
    @Override
    public List<Book> search(String text, String genre, Integer yearFrom, Integer yearTo) {
        StringBuilder sql  = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object>  args = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            sql.append("AND (LOWER(b.title) LIKE LOWER(?) " +
                    "     OR LOWER(a.full_name) LIKE LOWER(?) " +
                    "     OR LOWER(COALESCE(b.isbn,'')) LIKE LOWER(?)) ");
            String p = "%" + text.trim() + "%";
			args.add(p); args.add(p); args.add(p);
        }
        if (genre != null && !genre.isBlank()) {
            sql.append("AND b.genre = ? ");
            args.add(genre);
        }
        if (yearFrom != null && yearFrom > 0) {
            sql.append("AND b.pub_year >= ? ");
            args.add(yearFrom);
        }
        if (yearTo != null && yearTo > 0) {
            sql.append("AND b.pub_year <= ? ");
            args.add(yearTo);
        }
        sql.append("ORDER BY b.id");

        return query(sql.toString(), args);
    }

    // ── Write ─────────────────────────────────────────────────────

    @Override
    public void insert(Book b) {
        try (PreparedStatement ps = conn().prepareStatement(INSERT)) {
            ps.setString(1, b.getTitle());
            ps.setInt   (2, b.getAuthorId());
            setNullableString(ps, 3, b.getGenre());
            setNullableString(ps, 4, b.getIsbn());
            setNullableInt   (ps, 5, b.getPubYear());
            ps.setInt(6, b.getCopies());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BookDao.insert: " + e.getMessage());
            throw new RuntimeException("Failed to add book: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Book b) {
        try (PreparedStatement ps = conn().prepareStatement(UPDATE)) {
            ps.setString(1, b.getTitle());
            ps.setInt   (2, b.getAuthorId());
            setNullableString(ps, 3, b.getGenre());
            setNullableString(ps, 4, b.getIsbn());
            setNullableInt   (ps, 5, b.getPubYear());
            ps.setInt(6, b.getCopies());
            ps.setInt(7, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BookDao.update: " + e.getMessage());
            throw new RuntimeException("Failed to update book: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the book is referenced by any loan record, PostgreSQL raises
     * SQL state {@code 23503}. This is caught and re-thrown as a
     * {@link RuntimeException} with a human-readable message so the
     * controller can show an error dialog instead of silently failing.
     */
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BookDao.delete: " + e.getMessage());
            if (FK_VIOLATION.equals(e.getSQLState())) {
                throw new RuntimeException(
                        "Cannot delete this book — it is referenced by one or more loans.\n" +
                                "Delete or return all related loans first.", e);
            }
            throw new RuntimeException("Failed to delete book: " + e.getMessage(), e);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────

    /**
     * Executes a parameterized SELECT and maps all rows to {@link Book} objects.
     *
     * <p>Supports parameter types {@link String} and {@link Integer};
     * other types will be ignored (should not occur in practice).
     *
     * @param sql  the fully formed SQL query string
     * @param args ordered list of bind values
     * @return list of matched books; never {@code null}
     */
    private List<Book> query(String sql, List<Object> args) {
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                Object v = args.get(i);
                if (v instanceof String)  ps.setString(i + 1, (String)  v);
                else if (v instanceof Integer) ps.setInt(i + 1, (Integer) v);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("BookDao.query: " + e.getMessage());
        }
        return list;
    }

    /**
     * Binds a {@link String} parameter or sets it to SQL NULL if blank.
     *
     * @param ps the prepared statement
     * @param i  1-based parameter index
     * @param v  the string value; {@code null} or blank → SQL NULL
     */
    private void setNullableString(PreparedStatement ps, int i, String v)
            throws SQLException {
        if (v == null || v.isBlank()) ps.setNull(i, Types.VARCHAR);
        else                          ps.setString(i, v);
    }

    /**
     * Binds an int parameter or sets it to SQL NULL when the value is {@code 0}
     * (sentinel for "not set", following the model convention).
     *
     * @param ps the prepared statement
     * @param i  1-based parameter index
     * @param v  the int value; {@code 0} → SQL NULL
     */
    private void setNullableInt(PreparedStatement ps, int i, int v)
            throws SQLException {
        if (v == 0) ps.setNull(i, Types.SMALLINT);
        else        ps.setInt(i, v);
    }
}