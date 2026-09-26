package com.banking.controller;

import com.banking.util.DBConnection;
import com.banking.util.Input;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/customer/statement")
public class StatementServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try (Connection connection = DBConnection.getConnection()) {

            int customerId = Input.id(
                    req.getSession()
                            .getAttribute("userId")
                            .toString());

            int requestId = Input.id(req.getParameter("id"));

            long accountNumber;

            String requestSql = "SELECT account_number " +
                    "FROM service_request " +
                    "WHERE request_id = ? " +
                    "AND customer_id = ? " +
                    "AND request_type = 'ACCOUNT_STATEMENT' " +
                    "AND status = 'COMPLETED'";

            try (PreparedStatement ps = connection.prepareStatement(requestSql)) {

                ps.setInt(1, requestId);
                ps.setInt(2, customerId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        res.sendError(404);
                        return;
                    }

                    accountNumber = rs.getLong("account_number");
                }
            }

            res.setContentType(
                    "text/csv;charset=UTF-8");

            res.setHeader(
                    "Content-Disposition",
                    "attachment; filename=account-statement-" +
                            requestId +
                            ".csv");

            PrintWriter out = res.getWriter();

            out.println(
                    "Account,Date,Type,Direction," +
                            "Recipient,Amount,Status,Reference");

            String statementSql = "SELECT " +
                    "payment_date, " +
                    "payment_type, " +

                    "CASE " +

                    // Money entering the account
                    "WHEN payment_type IN (" +
                    "'LOAN_DISBURSEMENT'," +
                    "'INVESTMENT_PAYOUT'," +
                    "'CASH_DEPOSIT'" +
                    ") THEN 'CREDIT' " +

                    // Cash withdrawal
                    "WHEN payment_type = 'CASH_WITHDRAWAL' " +
                    "THEN 'DEBIT' " +

                    // Incoming transfer
                    "WHEN payment_type = 'TRANSFER' " +
                    "AND account_number <> ? " +
                    "THEN 'CREDIT' " +

                    // Everything else is money leaving the account
                    "ELSE 'DEBIT' " +

                    "END AS direction, " +

                    "recipient, " +
                    "amount, " +
                    "status, " +
                    "reference_number " +

                    "FROM payment " +

                    "WHERE account_number = ? " +

                    "OR (" +
                    "payment_type = 'TRANSFER' " +
                    "AND status = 'COMPLETED' " +
                    "AND CAST(recipient AS BINARY) = " +
                    "CAST(? AS BINARY)" +
                    ") " +

                    "ORDER BY payment_date, payment_id";

            try (PreparedStatement ps = connection.prepareStatement(statementSql)) {

                ps.setLong(1, accountNumber);
                ps.setLong(2, accountNumber);
                ps.setString(
                        3,
                        Long.toString(accountNumber));

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        out.print(accountNumber);

                        for (int i = 1; i <= 7; i++) {

                            out.print(',');

                            out.print(
                                    csv(rs.getString(i)));
                        }

                        out.println();
                    }
                }
            }

        } catch (IllegalArgumentException e) {

            res.sendError(
                    400,
                    "Invalid statement request.");

        } catch (SQLException e) {

            log(
                    "Statement export failed",
                    e);

            if (!res.isCommitted()) {

                res.reset();

                res.sendError(
                        503,
                        "Statement unavailable.");
            }
        }
    }

    private String csv(String value) {

        if (value == null) {
            return "\"\"";
        }

        /*
         * Protect CSV files against formula injection
         * when opened using spreadsheet applications.
         */
        if (value.matches("^[=+@\\-].*")) {
            value = "'" + value;
        }

        return "\"" +
                value.replace("\"", "\"\"") +
                "\"";
    }
}