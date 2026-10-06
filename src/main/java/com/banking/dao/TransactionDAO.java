package com.banking.dao;

import com.banking.util.DBConnection;

import java.sql.*;
import java.util.*;

public class TransactionDAO {

    public List<Map<String, Object>> history(
            int customerId,
            int limit,
            int offset) throws SQLException {

        String sql = "SELECT p.payment_id, " +
                "p.payment_type, " +
                "p.recipient, p.bill_reference, " +
                "p.amount, " +
                "p.payment_date, " +
                "p.status, " +
                "p.reference_number, " +

                "CASE " +

                // Money entering the customer's account
                "WHEN p.payment_type IN (" +
                "'LOAN_DISBURSEMENT'," +
                "'INVESTMENT_PAYOUT'," +
                "'CASH_DEPOSIT'" +
                ") THEN 'IN' " +

                // Cash withdrawal is always money leaving the account
                "WHEN p.payment_type = 'CASH_WITHDRAWAL' " +
                "THEN 'OUT' " +

                // Normal transaction created from customer's own account
                "WHEN a.customer_id = ? THEN 'OUT' " +

                // Incoming transfer
                "ELSE 'IN' " +

                "END AS direction " +

                "FROM payment p " +
                "JOIN account a " +
                "ON a.account_number = p.account_number " +

                "WHERE a.customer_id = ? " +

                "OR (" +
                "p.payment_type = 'TRANSFER' " +
                "AND p.status = 'COMPLETED' " +
                "AND EXISTS (" +
                "SELECT 1 " +
                "FROM account r " +
                "WHERE r.customer_id = ? " +
                "AND CAST(r.account_number AS BINARY) = " +
                "CAST(p.recipient AS BINARY)" +
                ")" +
                ") " +

                "ORDER BY p.payment_date DESC, " +
                "p.payment_id DESC " +

                "LIMIT ? OFFSET ?";

        try (
                Connection connection = DBConnection.getConnection();

                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            ps.setInt(3, customerId);
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                return rows(rs);
            }
        }
    }

    static List<Map<String, Object>> rows(
            ResultSet rs) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        ResultSetMetaData metadata = rs.getMetaData();

        int columnCount = metadata.getColumnCount();

        while (rs.next()) {

            Map<String, Object> row = new LinkedHashMap<>();

            for (int i = 1; i <= columnCount; i++) {

                row.put(
                        metadata.getColumnLabel(i),
                        rs.getObject(i));
            }

            rows.add(row);
        }

        return rows;
    }
}