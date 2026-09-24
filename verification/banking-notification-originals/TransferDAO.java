package com.banking.dao;

import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TransferDAO {

    public String transferMoney(
            int customerId,
            long receiverAccountNumber,
            BigDecimal amount) throws SQLException {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero.");
        }

        amount = com.banking.util.Input.money(amount);
        Connection con = null;

        try {

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // =========================================
            // 1. FIND SENDER ACCOUNT
            // =========================================

            long senderAccountNumber;
            BigDecimal senderBalance;

            String senderSql = "SELECT account_number, balance " +
                    "FROM account " +
                    "WHERE customer_id = ? " +
                    "AND status = 'ACTIVE' " +
                    "ORDER BY account_number " +
                    "LIMIT 1 FOR UPDATE";

            try (PreparedStatement ps = con.prepareStatement(senderSql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        throw new IllegalArgumentException(
                                "Your active bank account was not found.");
                    }

                    senderAccountNumber = rs.getLong("account_number");

                    senderBalance = rs.getBigDecimal("balance");
                }
            }

            // =========================================
            // 2. SAME ACCOUNT CHECK
            // =========================================

            if (senderAccountNumber == receiverAccountNumber) {

                throw new IllegalArgumentException(
                        "You cannot transfer money to the same account.");
            }

            // =========================================
            // 3. FIND RECEIVER ACCOUNT
            // =========================================

            String receiverSql = "SELECT account_number " +
                    "FROM account " +
                    "WHERE account_number = ? " +
                    "AND status = 'ACTIVE' " +
                    "FOR UPDATE";

            try (PreparedStatement ps = con.prepareStatement(receiverSql)) {

                ps.setLong(
                        1,
                        receiverAccountNumber);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        throw new IllegalArgumentException(
                                "Receiver account was not found or is not active.");
                    }
                }
            }

            // =========================================
            // 4. CHECK BALANCE
            // =========================================

            if (senderBalance == null ||
                    senderBalance.compareTo(amount) < 0) {

                throw new IllegalArgumentException(
                        "Insufficient account balance.");
            }

            // =========================================
            // 5. DEDUCT FROM SENDER
            // =========================================

            String deductSql = "UPDATE account " +
                    "SET balance = balance - ? " +
                    "WHERE account_number = ?";

            try (PreparedStatement ps = con.prepareStatement(deductSql)) {

                ps.setBigDecimal(1, amount);
                ps.setLong(2, senderAccountNumber);

                int rows = ps.executeUpdate();

                if (rows != 1) {

                    throw new SQLException(
                            "Unable to update sender balance.");
                }
            }

            // =========================================
            // 6. ADD TO RECEIVER
            // =========================================

            String addSql = "UPDATE account " +
                    "SET balance = balance + ? " +
                    "WHERE account_number = ?";

            try (PreparedStatement ps = con.prepareStatement(addSql)) {

                ps.setBigDecimal(1, amount);
                ps.setLong(
                        2,
                        receiverAccountNumber);

                int rows = ps.executeUpdate();

                if (rows != 1) {

                    throw new SQLException(
                            "Unable to update receiver balance.");
                }
            }

            // =========================================
            // 7. GENERATE REFERENCE NUMBER
            // =========================================

            String referenceNumber = "TRF-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")

                            .toUpperCase();

            // =========================================
            // 8. SAVE TRANSFER IN PAYMENT TABLE
            // =========================================

            String paymentSql = "INSERT INTO payment " +
                    "(account_number, payment_type, recipient, " +
                    "amount, status, reference_number) " +
                    "VALUES (?, 'TRANSFER', ?, ?, " +
                    "'COMPLETED', ?)";

            try (PreparedStatement ps = con.prepareStatement(paymentSql)) {

                ps.setLong(
                        1,
                        senderAccountNumber);

                ps.setString(
                        2,
                        String.valueOf(
                                receiverAccountNumber));

                ps.setBigDecimal(
                        3,
                        amount);

                ps.setString(
                        4,
                        referenceNumber);

                int rows = ps.executeUpdate();

                if (rows != 1) {

                    throw new SQLException(
                            "Unable to save transfer record.");
                }
            }

            // =========================================
            // 9. COMMIT
            // =========================================

            con.commit();

            return referenceNumber;

        } catch (SQLException | IllegalArgumentException e) {

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
}