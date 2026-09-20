package com.banking;

import com.banking.util.DBConnection;
import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            if (connection != null && !connection.isClosed()) {
                System.out.println("======================================");
                System.out.println("BANK DATABASE CONNECTED SUCCESSFULLY!");
                System.out.println("Database: banking_system");
                System.out.println("======================================");
            }

        } catch (Exception e) {
            System.err.println("DATABASE CONNECTION FAILED!");
            e.printStackTrace();
        }
    }
}