package com.library.dao.impl;

import com.library.dao.BookDao;
import com.library.db.DBConnection;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDaoImpl implements BookDao {

    // ── SQL ───────────────────────────────────────────────────────
    private static final String SELECT_BASE =
            "SELECT b.id, b.title, b.author_id, a.full_name AS author_name, " +
            "       b.genre, b.isbn, b.pub_year, b.copies " +
            "FROM books b JOIN authors a ON a.id = b.author_id ";

    private static final String FIND_ALL   = SELECT_BASE + "ORDER BY b.id";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE b.id = ?";

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

    // ── Helpers ───────────────────────────────────────────────────
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

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
     * Динамічний пошук. Будуємо WHERE-рядок залежно від заповнених параметрів:
     *   text     → LIKE в title та author full_name (одночасно OR)
     *   genre    → точне співпадіння
     *   yearFrom → pub_year >= yearFrom
     *   yearTo   → pub_year <= yearTo
     */
    @Override
    public List<Book> search(String text, String genre, Integer yearFrom, Integer yearTo) {
        StringBuilder sql  = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object>  args = new ArrayList<>();

        if (text != null && !text.isBlank()) {
            sql.append("AND (LOWER(b.title) LIKE LOWER(?) " +
                       "     OR LOWER(a.full_name) LIKE LOWER(?)) ");
            String p = "%" + text.trim() + "%";
            args.add(p);
            args.add(p);
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
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BookDao.delete: " + e.getMessage());
        }
    }

    // ── Internal ──────────────────────────────────────────────────
    /** Виконує запит із довільним списком параметрів (String | Integer). */
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

    private void setNullableString(PreparedStatement ps, int i, String v)
            throws SQLException {
        if (v == null || v.isBlank()) ps.setNull(i, Types.VARCHAR);
        else                          ps.setString(i, v);
    }

    private void setNullableInt(PreparedStatement ps, int i, int v)
            throws SQLException {
        if (v == 0) ps.setNull(i, Types.SMALLINT);
        else        ps.setInt(i, v);
    }
}
