package com.banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/banking_system"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Colombo";

    private static final String USERNAME = "root";

    // Put your MySQL password here
    private static final String PASSWORD = "1234";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("bank.db.url", System.getenv().getOrDefault("BANK_DB_URL", URL)),
                System.getProperty("bank.db.user", System.getenv().getOrDefault("BANK_DB_USER", USERNAME)),
                System.getProperty("bank.db.password", System.getenv().getOrDefault("BANK_DB_PASSWORD", PASSWORD)));
    }
}
