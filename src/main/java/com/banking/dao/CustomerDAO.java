package com.banking.dao;

import com.banking.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDAO {

    public boolean emailExists(String email) throws SQLException {

        String sql = "SELECT customer_id FROM customer WHERE email = ? UNION ALL SELECT employee_id FROM employee WHERE email = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean phoneExists(String phone) throws SQLException {

        String sql = "SELECT customer_id FROM customer WHERE phone = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int createCustomerAndAccount(
            String name,
            String address,
            String city,
            String postalCode,
            String phone,
            String email,
            String dateOfBirth,
            String password,
            String accountType) throws SQLException {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            // Customer + account must succeed together
            con.setAutoCommit(false);

            // ========================================
            // 1. INSERT CUSTOMER
            // ========================================

            String customerSQL = "INSERT INTO customer " +
                    "(name, address, city, postal_code, phone, email, " +
                    "date_of_birth, password, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

            int customerId;

            try (
                    PreparedStatement ps = con.prepareStatement(
                            customerSQL,
                            Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, name);
                ps.setString(2, emptyToNull(address));
                ps.setString(3, emptyToNull(city));
                ps.setString(4, emptyToNull(postalCode));
                ps.setString(5, phone);
                ps.setString(6, email);

                if (dateOfBirth == null ||
                        dateOfBirth.trim().isEmpty()) {

                    ps.setNull(7, java.sql.Types.DATE);

                } else {

                    ps.setDate(
                            7,
                            java.sql.Date.valueOf(dateOfBirth));
                }

                ps.setString(8, password);

                int rows = ps.executeUpdate();

                if (rows != 1) {
                    throw new SQLException(
                            "Customer creation failed.");
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {

                    if (!keys.next()) {

                        throw new SQLException(
                                "Customer ID was not generated.");
                    }

                    customerId = keys.getInt(1);
                }
            }

            // ========================================
            // 2. GENERATE ACCOUNT NUMBER
            // ========================================

            long accountNumber = generateAccountNumber(customerId);

            // ========================================
            // 3. CREATE BANK ACCOUNT
            // ========================================

            String accountSQL = "INSERT INTO account " +
                    "(account_number, customer_id, account_type, " +
                    "balance, status, open_date) " +
                    "VALUES (?, ?, ?, 0.00, 'ACTIVE', CURDATE())";

            try (
                    PreparedStatement ps = con.prepareStatement(accountSQL)) {

                ps.setLong(1, accountNumber);

                ps.setInt(2, customerId);

                ps.setString(
                        3,
                        accountType.toUpperCase());

                int rows = ps.executeUpdate();

                if (rows != 1) {

                    throw new SQLException(
                            "Bank account creation failed.");
                }
            }

            // Everything successful
            NotificationDAO.accountChanged(con,accountNumber);
            NotificationDAO.registered(con,customerId);
            con.commit();

            return customerId;

        } catch (SQLException | RuntimeException e) {

            if (con != null) {

                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }

            throw e;

        } finally {

            if (con != null) {

                try {

                    con.setAutoCommit(true);
                    con.close();

                } catch (SQLException ignored) {
                }
            }
        }
    }

    private long generateAccountNumber(int customerId) {

        /*
         * Example:
         *
         * Customer ID = 1
         * Account Number = 1000000001
         *
         * Customer ID = 25
         * Account Number = 1000000025
         */

        return 1_000_000_000L + customerId;
    }

    private String emptyToNull(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }
}