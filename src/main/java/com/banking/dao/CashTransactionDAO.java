package com.banking.dao;

import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * LankaTrust Banking System
 *
 * Handles employee-assisted Cash Deposits and Cash Withdrawals.
 *
 * Only an ACTIVE CUSTOMER_SERVICE_OFFICER can perform these
 * teller-style transactions.
 *
 * Every successful transaction:
 * 1. Locks the account.
 * 2. Validates account status.
 * 3. Updates the balance.
 * 4. Creates a payment history record.
 * 5. Creates an audit log.
 * 6. Commits everything as one database transaction.
 */
public class CashTransactionDAO {

    private static final String REQUIRED_ROLE = "CUSTOMER_SERVICE_OFFICER";

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999.99");

    /**
     * Cash Deposit
     */
    public String deposit(
            int employeeId,
            long accountNumber,
            BigDecimal amount,
            String note) throws SQLException {

        validateAmount(amount);

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                validateEmployee(connection, employeeId);

                AccountInfo account = lockAccount(connection, accountNumber);

                validateActiveAccount(account);

                String reference = generateReference("DEP");

                String updateSql = "UPDATE account " +
                        "SET balance = balance + ? " +
                        "WHERE account_number = ? " +
                        "AND status = 'ACTIVE'";

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {

                    ps.setBigDecimal(1, amount);
                    ps.setLong(2, accountNumber);

                    int updated = ps.executeUpdate();

                    if (updated != 1) {
                        throw new SQLException(
                                "Unable to deposit money into the account.");
                    }
                }

                recordPayment(
                        connection,
                        accountNumber,
                        "CASH_DEPOSIT",
                        "Cash Deposit",
                        amount,
                        reference);

                recordAudit(
                        connection,
                        employeeId,
                        "CASH_DEPOSIT",
                        accountNumber,
                        amount,
                        reference,
                        note);

                connection.commit();

                return reference;

            } catch (Exception e) {

                rollback(connection);

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }
                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                }

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        e.getMessage(),
                        e);

            } finally {

                restoreAutoCommit(connection);
            }
        }
    }

    /**
     * Cash Withdrawal
     */
    public String withdraw(
            int employeeId,
            long accountNumber,
            BigDecimal amount,
            String note) throws SQLException {

        validateAmount(amount);

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                validateEmployee(connection, employeeId);

                AccountInfo account = lockAccount(connection, accountNumber);

                validateActiveAccount(account);

                if (account.balance.compareTo(amount) < 0) {
                    throw new IllegalArgumentException(
                            "Insufficient account balance.");
                }

                String reference = generateReference("WDL");

                String updateSql = "UPDATE account " +
                        "SET balance = balance - ? " +
                        "WHERE account_number = ? " +
                        "AND status = 'ACTIVE' " +
                        "AND balance >= ?";

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {

                    ps.setBigDecimal(1, amount);
                    ps.setLong(2, accountNumber);
                    ps.setBigDecimal(3, amount);

                    int updated = ps.executeUpdate();

                    if (updated != 1) {
                        throw new IllegalArgumentException(
                                "Insufficient balance or account is unavailable.");
                    }
                }

                recordPayment(
                        connection,
                        accountNumber,
                        "CASH_WITHDRAWAL",
                        "Cash Withdrawal",
                        amount,
                        reference);

                recordAudit(
                        connection,
                        employeeId,
                        "CASH_WITHDRAWAL",
                        accountNumber,
                        amount,
                        reference,
                        note);

                connection.commit();

                return reference;

            } catch (Exception e) {

                rollback(connection);

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }
                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                }

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        e.getMessage(),
                        e);

            } finally {

                restoreAutoCommit(connection);
            }
        }
    }

    /**
     * Checks that the employee exists, is ACTIVE,
     * and is a CUSTOMER_SERVICE_OFFICER.
     */
    private void validateEmployee(
            Connection connection,
            int employeeId) throws SQLException {

        String sql = "SELECT role, status " +
                "FROM employee " +
                "WHERE employee_id = ? " +
                "FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Employee account was not found.");
                }

                String role = rs.getString("role");

                String status = rs.getString("status");

                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    throw new SecurityException(
                            "Employee account is not active.");
                }

                if (!REQUIRED_ROLE.equals(role)) {
                    throw new SecurityException(
                            "You are not authorized to perform cash transactions.");
                }
            }
        }
    }

    /**
     * Locks the selected account until commit/rollback.
     */
    private AccountInfo lockAccount(
            Connection connection,
            long accountNumber) throws SQLException {

        String sql = "SELECT customer_id, balance, status " +
                "FROM account " +
                "WHERE account_number = ? " +
                "FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Account number was not found.");
                }

                return new AccountInfo(
                        rs.getInt("customer_id"),
                        rs.getBigDecimal("balance"),
                        rs.getString("status"));
            }
        }
    }

    /**
     * Cash operations are only allowed on ACTIVE accounts.
     *
     * This prevents transactions on CLOSED, INACTIVE
     * and FROZEN accounts.
     */
    private void validateActiveAccount(
            AccountInfo account) {

        if (!"ACTIVE".equalsIgnoreCase(account.status)) {

            throw new IllegalArgumentException(
                    "Cash transactions are allowed only for ACTIVE accounts.");
        }
    }

    /**
     * Stores the cash transaction in the existing
     * payment table used as transaction history.
     */
    private void recordPayment(
            Connection connection,
            long accountNumber,
            String paymentType,
            String recipient,
            BigDecimal amount,
            String reference) throws SQLException {

        String sql = "INSERT INTO payment " +
                "(account_number, payment_type, recipient, amount, " +
                "payment_date, scheduled_date, status, reference_number) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, NULL, " +
                "'COMPLETED', ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, accountNumber);
            ps.setString(2, paymentType);
            ps.setString(3, recipient);
            ps.setBigDecimal(4, amount);
            ps.setString(5, reference);

            int inserted = ps.executeUpdate();

            if (inserted != 1) {

                throw new SQLException(
                        "Unable to create transaction history record.");
            }
        }
    }

    /**
     * Stores an employee audit trail.
     */
    private void recordAudit(
            Connection connection,
            int employeeId,
            String action,
            long accountNumber,
            BigDecimal amount,
            String reference,
            String note) throws SQLException {

        StringBuilder details = new StringBuilder();

        details.append("Account: ")
                .append(accountNumber)
                .append(" | Amount: LKR ")
                .append(amount.toPlainString())
                .append(" | Reference: ")
                .append(reference);

        if (note != null && !note.trim().isEmpty()) {

            details.append(" | Note: ")
                    .append(limit(note.trim(), 200));
        }

        String sql = "INSERT INTO audit_log " +
                "(employee_id, action, details, action_time) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setString(2, action);
            ps.setString(
                    3,
                    limit(details.toString(), 500));

            int inserted = ps.executeUpdate();

            if (inserted != 1) {

                throw new SQLException(
                        "Unable to create audit log.");
            }
        }
    }

    /**
     * Financial amount validation.
     */
    private void validateAmount(
            BigDecimal amount) {

        if (amount == null) {

            throw new IllegalArgumentException(
                    "Transaction amount is required.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero.");
        }

        if (amount.scale() > 2) {

            throw new IllegalArgumentException(
                    "Maximum two decimal places are allowed.");
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {

            throw new IllegalArgumentException(
                    "Transaction amount exceeds the allowed limit.");
        }
    }

    /**
     * Generates unique transaction reference.
     */
    private String generateReference(
            String prefix) {

        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return prefix + "-" + random;
    }

    /**
     * Limits text before saving to VARCHAR columns.
     */
    private String limit(
            String value,
            int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength);
    }

    /**
     * Safe rollback.
     */
    private void rollback(
            Connection connection) {

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    /**
     * Restore JDBC connection state.
     */
    private void restoreAutoCommit(
            Connection connection) {

        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    /**
     * Internal locked account information.
     */
    private static class AccountInfo {

        private final int customerId;
        private final BigDecimal balance;
        private final String status;

        private AccountInfo(
                int customerId,
                BigDecimal balance,
                String status) {

            this.customerId = customerId;
            this.balance = balance;
            this.status = status;
        }
    }
}