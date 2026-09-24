package com.banking.dao;

import com.banking.util.*;
import java.sql.*;
import java.util.*;

public class EmployeeDAO {

    public Map<String, List<Map<String, Object>>> dashboard(String role) throws SQLException {
        return dashboard(role, null);
    }

    public Map<String, List<Map<String, Object>>> dashboard(String role, String search) throws SQLException {

        Map<String, String> queries = new LinkedHashMap<>();

        switch (role) {

            /*
             * ============================================================
             * LOAN OFFICER
             * ============================================================
             */
            case "LOAN_OFFICER" -> {

                queries.put(
                        "Loans",
                        """
                                SELECT
                                    loan_id,
                                    customer_id,
                                    employee_id,
                                    loan_type,
                                    amount,
                                    interest_rate,
                                    term_months,
                                    status,
                                    rejection_reason
                                FROM loan
                                ORDER BY loan_id DESC
                                LIMIT 200
                                """);

                queries.put(
                        "Repayments",
                        """
                                SELECT
                                    repayment_id,
                                    loan_id,
                                    amount,
                                    due_date,
                                    paid_date,
                                    status
                                FROM loan_repayment
                                ORDER BY due_date DESC
                                LIMIT 200
                                """);
            }

            /*
             * ============================================================
             * CARD SERVICES OFFICER
             *
             * Card information is joined with Account and Customer so the
             * officer can clearly identify who owns each card.
             * ============================================================
             */
            case "CARD_SERVICES_OFFICER" -> queries.put(
                    "Cards",
                    """
                            SELECT
                                c.card_id,
                                cu.customer_id,
                                cu.name AS customer_name,
                                cu.email AS customer_email,
                                cu.phone AS customer_phone,
                                c.account_number,
                                CONCAT('**** ', RIGHT(c.card_number, 4)) AS card_number,
                                c.card_type,
                                c.status,
                                c.expiry_date,
                                c.daily_limit
                            FROM card c
                            INNER JOIN account a
                                ON a.account_number = c.account_number
                            INNER JOIN customer cu
                                ON cu.customer_id = a.customer_id
                            ORDER BY c.card_id DESC
                            LIMIT 200
                            """);

            /*
             * ============================================================
             * INVESTMENT OFFICER
             * ============================================================
             */
            case "INVESTMENT_OFFICER" -> queries.put(
                    "Investments",
                    """
                            SELECT
                                investment_id,
                                customer_id,
                                employee_id,
                                investment_type,
                                amount,
                                interest_rate,
                                start_date,
                                maturity_date,
                                status
                            FROM investment
                            ORDER BY investment_id DESC
                            LIMIT 200
                            """);

            /*
             * ============================================================
             * CUSTOMER SERVICE OFFICER
             * ============================================================
             */
            case "CUSTOMER_SERVICE_OFFICER" -> {

                queries.put(
                        "Service requests",
                        """
                                SELECT
                                    request_id,
                                    customer_id,
                                    request_type,
                                    description,
                                    status,
                                    rejection_reason,
                                    date_submitted
                                FROM service_request
                                ORDER BY request_id DESC
                                LIMIT 200
                                """);

            }

            /*
             * ============================================================
             * COMPLIANCE & RISK OFFICER
             * ============================================================
             */
            case "COMPLIANCE_RISK_OFFICER" -> {

                queries.put(
                        "Accounts",
                        """
                                SELECT
                                    account_number,
                                    customer_id,
                                    account_type,
                                    balance,
                                    status
                                FROM account
                                ORDER BY account_number
                                LIMIT 200
                                """);

                queries.put(
                        "Payments",
                        """
                                SELECT
                                    payment_id,
                                    account_number,
                                    payment_type,
                                    recipient,
                                    bill_reference,
                                    amount,
                                    status,
                                    payment_date,
                                    reference_number
                                FROM payment
                                ORDER BY payment_id DESC
                                LIMIT 200
                                """);

                queries.put(
                        "Audit log",
                        """
                                SELECT
                                    log_id,
                                    employee_id,
                                    action,
                                    details,
                                    action_time
                                FROM audit_log
                                ORDER BY log_id DESC
                                LIMIT 200
                                """);
            }

            /*
             * ============================================================
             * SYSTEM ADMINISTRATOR
             * ============================================================
             */
            case "SYSTEM_ADMIN" -> {

                queries.put(
                        "Customers",
                        """
                                SELECT
                                    customer_id,
                                    name,
                                    email,
                                    phone,
                                    status
                                FROM customer
                                ORDER BY customer_id
                                LIMIT 200
                                """);

                queries.put(
                        "Employees",
                        """
                                SELECT
                                    employee_id,
                                    name,
                                    email,
                                    phone,
                                    role,
                                    status
                                FROM employee
                                ORDER BY employee_id
                                LIMIT 200
                                """);

                queries.put(
                        "Products",
                        """
                                SELECT
                                    product_id,
                                    product_name,
                                    product_type,
                                    description,
                                    status
                                FROM banking_product
                                ORDER BY product_id
                                LIMIT 200
                                """);

                queries.put(
                        "Audit log",
                        """
                                SELECT
                                    log_id,
                                    employee_id,
                                    action,
                                    details,
                                    action_time
                                FROM audit_log
                                ORDER BY log_id DESC
                                LIMIT 200
                                """);
            }

            default -> throw new IllegalArgumentException("Invalid role.");
        }

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        try (Connection c = DBConnection.getConnection()) {

            for (var query : queries.entrySet()) {

                String sql = query.getValue();

                boolean filter = "Audit log".equals(query.getKey())
                        && search != null
                        && !search.isBlank();

                if (filter) {
                    sql = sql.replace(
                            "ORDER BY",
                            "WHERE LOWER(action) LIKE LOWER(?) ESCAPE '=' OR LOWER(details) LIKE LOWER(?) ESCAPE '=' ORDER BY");
                }

                try (PreparedStatement ps = c.prepareStatement(sql)) {

                    if (filter) {

                        String keyword = Input.text(search, 100, "search");
                        String pattern = "%" + keyword.replace("=", "==").replace("%", "=%").replace("_", "=_") + "%";

                        ps.setString(1, pattern);
                        ps.setString(2, pattern);
                    }

                    try (ResultSet rs = ps.executeQuery()) {

                        var records = TransactionDAO.rows(rs);
                        result.put(query.getKey(),records);
                    }
                }
            }
        }

        return result;
    }

    /*
     * ================================================================
     * EMPLOYEE ACTIONS
     * ================================================================
     */
    public void act(
            int employeeId,
            String role,
            String action,
            String idValue,
            String status,
            String reason) throws SQLException {

        if ("ticket-response".equals(action)) {
            new TicketDAO().update(employeeId,Input.id(idValue),action,status,reason);
            return;
        }

        reason = Input.text(
                reason,
                500,
                "reason or response");

        String sql;

        switch (role) {

            /*
             * ========================================================
             * LOAN OFFICER
             * ========================================================
             */
            case "LOAN_OFFICER" -> {

                Input.choice(
                        action,
                        "loan-reject");

                status = "REJECTED";

                sql = "UPDATE loan " +
                        "SET status=?, rejection_reason=?, employee_id=? " +
                        "WHERE loan_id=? AND status='PENDING'";
            }

            /*
             * ========================================================
             * CARD SERVICES OFFICER
             * ========================================================
             */
            case "CARD_SERVICES_OFFICER" -> {

                Input.choice(
                        action,
                        "card-block");

                status = "BLOCKED";

                sql = "UPDATE card " +
                        "SET status=? " +
                        "WHERE card_id=? AND status='ACTIVE'";
            }

            /*
             * ========================================================
             * INVESTMENT OFFICER
             * ========================================================
             */
            case "INVESTMENT_OFFICER" -> {

                Input.choice(
                        action,
                        "investment-assign");

                sql = "UPDATE investment " +
                        "SET employee_id=? " +
                        "WHERE investment_id=? " +
                        "AND status='PENDING' " +
                        "AND employee_id IS NULL";
            }

            /*
             * ========================================================
             * CUSTOMER SERVICE OFFICER
             * ========================================================
             */
            case "CUSTOMER_SERVICE_OFFICER" -> {

                Input.choice(action,"request-review");
                status=Input.choice(status,"PROCESSING","REJECTED");
                sql="UPDATE service_request SET status=?, rejection_reason=? WHERE request_id=? AND status IN ('PENDING','PROCESSING')";
            }

            /*
             * ========================================================
             * COMPLIANCE & RISK OFFICER
             * ========================================================
             */
            case "COMPLIANCE_RISK_OFFICER" -> {

                Input.choice(
                        action,
                        "account-status");

                status = Input.choice(
                        status,
                        "ACTIVE",
                        "FROZEN");

                sql = "UPDATE account " +
                        "SET status=? " +
                        "WHERE account_number=? " +
                        "AND status=?";
            }

            /*
             * ========================================================
             * SYSTEM ADMINISTRATOR
             * ========================================================
             */
            case "SYSTEM_ADMIN" -> {

                Input.choice(
                        action,
                        "employee-status");

                status = Input.choice(
                        status,
                        "ACTIVE",
                        "INACTIVE");

                if (Input.id(idValue) == employeeId) {

                    throw new IllegalArgumentException(
                            "You cannot change your own access status.");
                }

                sql = "UPDATE employee " +
                        "SET status=? " +
                        "WHERE employee_id=? " +
                        "AND status<>?";
            }

            default ->
                throw new IllegalArgumentException(
                        "Operation not permitted.");
        }

        /*
         * ================================================================
         * TRANSACTION
         * ================================================================
         */
        try (Connection c = DBConnection.getConnection()) {

            c.setAutoCommit(false);

            try {

                /*
                 * Recheck staff access inside the same transaction
                 * as the requested action.
                 */
                try (
                        PreparedStatement ps = c.prepareStatement(
                                "SELECT role " +
                                        "FROM employee " +
                                        "WHERE employee_id=? " +
                                        "AND status='ACTIVE' " +
                                        "FOR UPDATE")) {

                    ps.setInt(
                            1,
                            employeeId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()
                                || !role.equals(
                                        rs.getString(1))) {

                            throw new IllegalArgumentException(
                                    "Employee access has changed. Sign in again.");
                        }
                    }
                }

                String previousRequestStatus="request-review".equals(action)
                        ?(String)Jdbc.one(c,"SELECT status FROM service_request WHERE request_id=? FOR UPDATE",Input.id(idValue)).get("status"):null;
                /*
                 * Execute role-specific action.
                 */
                try (
                        PreparedStatement ps = c.prepareStatement(sql)) {

                    if ("loan-reject".equals(action)) {

                        ps.setString(
                                1,
                                status);

                        ps.setString(
                                2,
                                reason);

                        ps.setInt(
                                3,
                                employeeId);

                        ps.setInt(
                                4,
                                Input.id(idValue));

                    } else if ("request-review".equals(action)) {

                        ps.setString(
                                1,
                                status);

                        ps.setString(
                                2,
                                "REJECTED".equals(status)
                                        ? reason
                                        : null);

                        ps.setInt(
                                3,
                                Input.id(idValue));

                    } else if ("investment-assign".equals(action)) {

                        ps.setInt(
                                1,
                                employeeId);

                        ps.setInt(
                                2,
                                Input.id(idValue));

                    } else if ("account-status".equals(action)) {

                        ps.setString(
                                1,
                                status);

                        try {

                            ps.setLong(
                                    2,
                                    Long.parseLong(idValue));

                        } catch (RuntimeException e) {

                            throw new IllegalArgumentException(
                                    "Invalid account number.");
                        }

                        ps.setString(
                                3,
                                "ACTIVE".equals(status)
                                        ? "FROZEN"
                                        : "ACTIVE");

                    } else {

                        ps.setString(
                                1,
                                status);

                        ps.setInt(
                                2,
                                Input.id(idValue));

                        if ("employee-status".equals(action)) {

                            ps.setString(
                                    3,
                                    status);
                        }
                    }

                    if (ps.executeUpdate() != 1) {

                        throw new IllegalArgumentException(
                                "Record is not eligible or has already changed. " +
                                        "Refresh and try again.");
                    }
                }

                /*
                 * ========================================================
                 * AUDIT LOG
                 * ========================================================
                 */
                try (
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO audit_log" +
                                        "(employee_id,action,details) " +
                                        "VALUES(?,?,?)")) {

                    ps.setInt(
                            1,
                            employeeId);

                    ps.setString(
                            2,
                            action);

                    String details = "Record " + idValue +
                            "; status=" + status +
                            "; " + reason;

                    ps.setString(
                            3,
                            details.substring(
                                    0,
                                    Math.min(
                                            500,
                                            details.length())));

                    if (ps.executeUpdate() != 1) {

                        throw new SQLException(
                                "Audit insert failed");
                    }
                }

                switch(action) {
                    case "request-review" -> {if(!status.equals(previousRequestStatus))NotificationDAO.changed(c,NotificationDAO.Product.REQUEST,Input.id(idValue));}
                    case "account-status" -> NotificationDAO.accountChanged(c,Long.parseLong(idValue));
                    case "employee-status" -> NotificationDAO.employeeAccess(c,Input.id(idValue));
                    case "loan-reject" -> NotificationDAO.changed(c,NotificationDAO.Product.LOAN,Input.id(idValue));
                    case "card-block" -> NotificationDAO.changed(c,NotificationDAO.Product.CARD,Input.id(idValue));
                    default -> { }
                }
                c.commit();

            } catch (
                    SQLException | RuntimeException e) {

                c.rollback();

                throw e;
            }
        }
    }
}