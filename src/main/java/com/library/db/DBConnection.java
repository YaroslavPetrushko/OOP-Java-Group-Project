package com.library.db;

import java.sql.Connection;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        // stub — JDBC connect logic goes here
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        // stub
        return connection;
    }
}