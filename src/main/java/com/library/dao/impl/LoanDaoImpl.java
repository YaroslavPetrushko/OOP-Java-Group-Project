package com.library.dao.impl;

import com.library.dao.LoanDao;
import com.library.db.DBConnection;
import com.library.model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link LoanDao}.
 *
 * <p>All queries are executed via {@link PreparedStatement} — no string
 * concatenation is used anywhere in this class.
 *
 * <p>The {@link #search} method applies smart text matching: a numeric
 * string triggers an exact ID lookup, while a non-numeric string triggers
 * a LIKE search across the book title and reader name.
 */
public class LoanDaoImpl implements LoanDao {

    // ── SQL constants ─────────────────────────────────────────────

    /**
     * Base SELECT with JOINs to resolve book title and reader name.
     * Used by all read methods to avoid repetition.
     */
    private static final String SELECT_BASE =
            "SELECT l.id, l.book_id, b.title AS book_title, " +
            "       l.reader_id, r.full_name AS reader_name, " +
            "       l.loan_date, l.due_date, l.status " +
            "FROM loans l " +
            "JOIN books   b ON b.id = l.book_id " +
            "JOIN readers r ON r.id = l.reader_id ";

    private static final String FIND_ALL   = SELECT_BASE + "ORDER BY l.id DESC";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE l.id=?";

    private static final String INSERT =
            "INSERT INTO loans (book_id, reader_id, loan_date, due_date, status) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE loans SET book_id=?, reader_id=?, loan_date=?, due_date=?, status=? " +
            "WHERE id=?";
    private static final String DELETE = "DELETE FROM loans WHERE id=?";

    // ── Connection helper ─────────────────────────────────────────

    /** @return the active JDBC connection from the singleton pool */
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    // ── Row mapper ────────────────────────────────────────────────

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Loan} object.
     *
     * @param rs the result set positioned on the row to map
     * @return a fully populated {@link Loan}
     * @throws SQLException if any column access fails
     */
    private Loan mapRow(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getString("book_title"),
                rs.getInt("reader_id"),
                rs.getString("reader_name"),
                rs.getDate("loan_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                rs.getString("status")
        );
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    public List<Loan> findAll() {
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("LoanDao.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Loan> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("LoanDao.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Builds and executes a dynamic WHERE clause.
     *
     * <p>Smart text dispatch: if {@code text} is a valid integer it is treated
     * as an exact loan ID; otherwise a LIKE search is performed against both the
     * book title and the reader name.
     *
     * {@inheritDoc}
     */
    @Override
    public List<Loan> search(String text, String status,
                             LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql  = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object>  args = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            String trimmed = text.trim();
            try {
                // Numeric input → exact ID match
                int id = Integer.parseInt(trimmed);
                sql.append("AND l.id = ? ");
                args.add(id);
            } catch (NumberFormatException ex) {
                // Non-numeric input → LIKE on title and reader name
                String p = "%" + trimmed + "%";
                sql.append("AND (LOWER(b.title) LIKE LOWER(?) " +
                           "     OR LOWER(r.full_name) LIKE LOWER(?)) ");
                args.add(p); args.add(p);
            }
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND l.status = ? ");
            args.add(status);
        }
        if (dateFrom != null) {
            sql.append("AND l.loan_date >= ? ");
            args.add(dateFrom);
        }
        if (dateTo != null) {
            sql.append("AND l.loan_date <= ? ");
            args.add(dateTo);
        }
        sql.append("ORDER BY l.id DESC");

        return query(sql.toString(), args);
    }

    // ── Write ─────────────────────────────────────────────────────

    @Override
    public void insert(Loan l) {
        try (PreparedStatement ps = conn().prepareStatement(INSERT)) {
            ps.setInt   (1, l.getBookId());
            ps.setInt   (2, l.getReaderId());
            ps.setDate  (3, Date.valueOf(l.getLoanDate()));
            ps.setDate  (4, Date.valueOf(l.getDueDate()));
            ps.setString(5, l.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("LoanDao.insert: " + e.getMessage());
            throw new RuntimeException("Failed to add loan: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Loan l) {
        try (PreparedStatement ps = conn().prepareStatement(UPDATE)) {
            ps.setInt   (1, l.getBookId());
            ps.setInt   (2, l.getReaderId());
            ps.setDate  (3, Date.valueOf(l.getLoanDate()));
            ps.setDate  (4, Date.valueOf(l.getDueDate()));
            ps.setString(5, l.getStatus());
            ps.setInt   (6, l.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("LoanDao.update: " + e.getMessage());
            throw new RuntimeException("Failed to update loan: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("LoanDao.delete: " + e.getMessage());
            throw new RuntimeException("Failed to delete loan: " + e.getMessage(), e);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────

    /**
     * Executes a parameterized SELECT and maps all rows to {@link Loan} objects.
     *
     * @param sql  the fully formed SQL query string
     * @param args ordered list of bind values ({@link String}, {@link Integer},
     *             or {@link LocalDate})
     * @return list of matched loans; never {@code null}
     */
    private List<Loan> query(String sql, List<Object> args) {
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                Object v = args.get(i);
                if      (v instanceof String)    ps.setString(i + 1, (String) v);
                else if (v instanceof Integer)   ps.setInt   (i + 1, (Integer) v);
                else if (v instanceof LocalDate) ps.setDate  (i + 1, Date.valueOf((LocalDate) v));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("LoanDao.query: " + e.getMessage());
        }
        return list;
    }
}