package com.banking.dao;

import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PaymentDAO {

    public static String validateBillReference(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter the bill / consumer account number.");
        }
        String reference = value.trim();
        if (reference.length() > 100 || !reference.matches("[A-Za-z0-9]+(?:[ /-][A-Za-z0-9]+)*")) {
            throw new IllegalArgumentException("Bill / consumer account number must be at most 100 characters, using letters, numbers, spaces, hyphens or slashes between groups.");
        }
        return reference;
    }

    public String makeBillPayment(
            int customerId,
            String recipient,
            String billReference,
            BigDecimal amount) throws SQLException {

        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter the bill or service provider.");
        }

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero.");
        }

        amount = com.banking.util.Input.money(amount);
        recipient = com.banking.util.Input.text(recipient, 150, "bill or service provider");
        billReference = validateBillReference(billReference);
        Connection con = null;

        try {

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            long accountNumber;
            BigDecimal balance;

            String accountSql = "SELECT account_number, balance " +
                    "FROM account " +
                    "WHERE customer_id = ? " +
                    "AND status = 'ACTIVE' " +
                    "ORDER BY account_number " +
                    "LIMIT 1 FOR UPDATE";

            try (PreparedStatement ps = con.prepareStatement(accountSql)) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "Your active bank account was not found.");
                    }

                    accountNumber = rs.getLong("account_number");

                    balance = rs.getBigDecimal("balance");
                }
            }

            if (balance == null ||
                    balance.compareTo(amount) < 0) {

                throw new IllegalArgumentException(
                        "Insufficient account balance.");
            }

            String updateSql = "UPDATE account " +
                    "SET balance = balance - ? " +
                    "WHERE account_number = ?";

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {

                ps.setBigDecimal(1, amount);
                ps.setLong(2, accountNumber);

                int rows = ps.executeUpdate();

                if (rows != 1) {
                    throw new SQLException(
                            "Unable to update account balance.");
                }
            }

            String referenceNumber = "PAY-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")

                            .toUpperCase();

            String paymentSql = "INSERT INTO payment " +
                    "(account_number, payment_type, recipient, " +
                    "amount, status, reference_number, bill_reference) " +
                    "VALUES (?, 'BILL_PAYMENT', ?, ?, " +
                    "'COMPLETED', ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(paymentSql)) {

                ps.setLong(1, accountNumber);
                ps.setString(2, recipient.trim());
                ps.setBigDecimal(3, amount);
                ps.setString(4, referenceNumber);
                ps.setString(5, billReference);

                int rows = ps.executeUpdate();

                if (rows != 1) {
                    throw new SQLException(
                            "Unable to save payment record.");
                }
            }

            NotificationDAO.payment(con,referenceNumber);
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