package com.library.dao.impl;

import com.library.dao.BookDao;
import com.library.db.DBConnection;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDaoImpl implements BookDao {

    private static final String SELECT_BASE =
            "SELECT b.id, b.title, b.author_id, a.full_name AS author_name, " +
                    "b.genre, b.isbn, b.pub_year, b.copies " +
                    "FROM books b JOIN authors a ON a.id = b.author_id ";

    private static final String FIND_ALL = SELECT_BASE + "ORDER BY b.title";
    private static final String FIND_BY_TITLE = SELECT_BASE + "WHERE LOWER(b.title) LIKE LOWER(?) ORDER BY b.title";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE b.id = ?";

    private static final String INSERT =
            "INSERT INTO books (title, author_id, genre, isbn, pub_year, copies) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE books SET title=?, author_id=?, genre=?, isbn=?, pub_year=?, copies=? WHERE id=?";

    private static final String DELETE = "DELETE FROM books WHERE id=?";

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getInt("author_id"),
                rs.getString("author_name"),
                rs.getString("genre"),
                rs.getString("isbn"),
                rs.getObject("pub_year", Integer.class),   // дозволяє NULL
                rs.getInt("copies")
        );
    }

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("BookDaoImpl.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Book> findByTitle(String title) {
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_TITLE)) {
            ps.setString(1, "%" + title.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("BookDaoImpl.findByTitle: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Book> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("BookDaoImpl.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void insert(Book book) {
        try (PreparedStatement ps = conn().prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getAuthorId());
            setNullableString(ps, 3, book.getGenre());
            setNullableString(ps, 4, book.getIsbn());
            setNullableInt(ps, 5, book.getPubYear());
            ps.setInt(6, book.getCopies());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    book.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("BookDaoImpl.insert: " + e.getMessage());
        }
    }

    @Override
    public void update(Book book) {
        try (PreparedStatement ps = conn().prepareStatement(UPDATE)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getAuthorId());
            setNullableString(ps, 3, book.getGenre());
            setNullableString(ps, 4, book.getIsbn());
            setNullableInt(ps, 5, book.getPubYear());
            ps.setInt(6, book.getCopies());
            ps.setInt(7, book.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BookDaoImpl.update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BookDaoImpl.delete: " + e.getMessage());
        }
    }

    // ── Допоміжні методи для NULL ─────────────────────────────────
    private void setNullableString(PreparedStatement ps, int paramIndex, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(paramIndex, Types.VARCHAR);
        } else {
            ps.setString(paramIndex, value.trim());
        }
    }

    private void setNullableInt(PreparedStatement ps, int paramIndex, Integer value) throws SQLException {
        if (value == null || value == 0) {
            ps.setNull(paramIndex, Types.SMALLINT);
        } else {
            ps.setInt(paramIndex, value);
        }
    }
}