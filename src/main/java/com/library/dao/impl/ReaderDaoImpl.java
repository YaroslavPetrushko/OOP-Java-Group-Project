package com.library.dao.impl;

import com.library.dao.ReaderDao;
import com.library.db.DBConnection;
import com.library.model.Reader;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderDaoImpl implements ReaderDao {

    // ── SQL ───────────────────────────────────────────────────────
    private static final String FIND_ALL =
            "SELECT id, full_name, email, phone, reg_date " +
                    "FROM readers ORDER BY full_name";

    private static final String FIND_BY_NAME =
            "SELECT id, full_name, email, phone, reg_date FROM readers " +
                    "WHERE LOWER(full_name) LIKE LOWER(?) ORDER BY full_name";

    private static final String FIND_BY_ID =
            "SELECT id, full_name, email, phone, reg_date FROM readers WHERE id=?";

    private static final String INSERT =
            "INSERT INTO readers (full_name, email, phone, reg_date) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE readers SET full_name=?, email=?, phone=?, reg_date=? WHERE id=?";

    private static final String DELETE =
            "DELETE FROM readers WHERE id=?";

    // ── Helpers ───────────────────────────────────────────────────
    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    private Reader mapRow(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("reg_date");
        LocalDate regDate = sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now();
        return new Reader(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                regDate
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
    public List<Reader> findByName(String name) {
        List<Reader> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_NAME)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("ReaderDao.findByName: " + e.getMessage());
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

    // ── Write ─────────────────────────────────────────────────────
    @Override
    public void insert(Reader r) {
        try (PreparedStatement ps = conn().prepareStatement(INSERT)) {
            ps.setString(1, r.getFullName());
            setNullableString(ps, 2, r.getEmail());
            setNullableString(ps, 3, r.getPhone());
            ps.setDate(4, Date.valueOf(
                    r.getRegDate() != null ? r.getRegDate() : LocalDate.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReaderDao.insert: " + e.getMessage());
        }
    }

    @Override
    public void update(Reader r) {
        try (PreparedStatement ps = conn().prepareStatement(UPDATE)) {
            ps.setString(1, r.getFullName());
            setNullableString(ps, 2, r.getEmail());
            setNullableString(ps, 3, r.getPhone());
            ps.setDate(4, Date.valueOf(
                    r.getRegDate() != null ? r.getRegDate() : LocalDate.now()));
            ps.setInt(5, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReaderDao.update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReaderDao.delete: " + e.getMessage());
        }
    }

    // ── NULL-safe setter ──────────────────────────────────────────
    private void setNullableString(PreparedStatement ps, int i, String v)
            throws SQLException {
        if (v == null || v.isBlank()) ps.setNull(i, Types.VARCHAR);
        else                          ps.setString(i, v);
    }
}