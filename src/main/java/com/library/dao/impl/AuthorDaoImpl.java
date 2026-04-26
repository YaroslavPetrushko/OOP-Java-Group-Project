package com.library.dao.impl;

import com.library.dao.AuthorDao;
import com.library.db.DBConnection;
import com.library.model.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorDaoImpl implements AuthorDao {

    // ── SQL ───────────────────────────────────────────────────────
    private static final String FIND_ALL =
            "SELECT id, full_name, country, birth_year " +
                    "FROM authors ORDER BY full_name";

    private static final String FIND_BY_NAME =
            "SELECT id, full_name, country, birth_year FROM authors " +
                    "WHERE LOWER(full_name) LIKE LOWER(?) ORDER BY full_name";

    private static final String FIND_BY_ID =
            "SELECT id, full_name, country, birth_year FROM authors WHERE id=?";

    private static final String INSERT =
            "INSERT INTO authors (full_name, country, birth_year) VALUES (?, ?, ?)";

    private static final String UPDATE =
            "UPDATE authors SET full_name=?, country=?, birth_year=? WHERE id=?";

    private static final String DELETE =
            "DELETE FROM authors WHERE id=?";

    // ── Helpers ───────────────────────────────────────────────────
    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    private Author mapRow(ResultSet rs) throws SQLException {
        return new Author(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("country"),
                rs.getInt("birth_year")   // returns 0 if NULL
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
    public List<Author> findByName(String name) {
        List<Author> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_NAME)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("AuthorDao.findByName: " + e.getMessage());
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

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AuthorDao.delete: " + e.getMessage());
        }
    }

    // ── NULL-safe setters ─────────────────────────────────────────
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