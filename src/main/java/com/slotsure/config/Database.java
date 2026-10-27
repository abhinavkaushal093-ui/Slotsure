package com.slotsure.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String url = requiredEnv("SLOTSURE_DB_URL");
        String user = requiredEnv("SLOTSURE_DB_USER");
        
        // Allow the password to be blank for local XAMPP environments
        String password = System.getenv("SLOTSURE_DB_PASSWORD");
        if (password == null) {
            password = ""; 
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Missing required environment variable: " + name
            );
        }

        return value;
    }
}