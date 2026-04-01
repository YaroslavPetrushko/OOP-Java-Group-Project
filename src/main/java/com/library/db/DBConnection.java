package com.library.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        // Connection settings from .env
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        try {
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("  ✅ Successful connection to database!");
        } catch (SQLException e) {
            System.err.println("  ❌ Connection error: " + e.getMessage());
        }
    }

    public static DBConnection getInstance() {
        try {
            // Checking if connection is still active
            if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        // stub
        return connection;
    }
}