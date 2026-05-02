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
    private static final String SELECT_BASE =
            "SELECT id, full_name, country, birth_year FROM authors ";

    private static final String FIND_ALL   = SELECT_BASE + "ORDER BY full_name";
    private static final String FIND_BY_ID = SELECT_BASE + "WHERE id=?";

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
     * text   → LIKE в full_name, country та CAST(birth_year AS TEXT)
     * country → точне співпадіння (окремий фільтр)
     * yearFrom/yearTo → birth_year діапазон
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

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AuthorDao.delete: " + e.getMessage());
        }
    }

    // ── Internal ──────────────────────────────────────────────────
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
