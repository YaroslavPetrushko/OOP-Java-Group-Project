package com.library.dao.impl;

import com.library.dao.AuthorDao;
import com.library.db.DBConnection;
import com.library.model.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDaoImpl implements AuthorDao {

    private static final String FIND_ALL =
            "SELECT id, full_name, country, birth_year " +
                    "FROM authors ORDER BY full_name";

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public List<Author> findAll() {
        List<Author> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Author(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("country"),
                        rs.getInt("birth_year")
                ));
            }
        } catch (SQLException e) {
            System.err.println("AuthorDao.findAll: " + e.getMessage());
        }
        return list;
    }
}