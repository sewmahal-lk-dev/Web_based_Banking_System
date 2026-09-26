package com.banking.dao;

import com.banking.model.Account;
import com.banking.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public List<Account> getAccountsByCustomerId(int customerId) {

        List<Account> accounts = new ArrayList<>();

        String sql = "SELECT account_number, customer_id, account_type, " +
                "balance, status, open_date " +
                "FROM account " +
                "WHERE customer_id = ? " +
                "ORDER BY account_number";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Account account = mapAccount(resultSet);

                    accounts.add(account);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error loading customer accounts: "
                            + e.getMessage());

            e.printStackTrace();
        }

        return accounts;
    }

    public Account getFirstAccountByCustomerId(int customerId) {

        String sql = "SELECT account_number, customer_id, account_type, " +
                "balance, status, open_date " +
                "FROM account " +
                "WHERE customer_id = ? " +
                "AND status = 'ACTIVE' ORDER BY account_number " +
                "LIMIT 1";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapAccount(resultSet);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error loading dashboard account: "
                            + e.getMessage());

            e.printStackTrace();
        }

        return null;
    }

    private Account mapAccount(ResultSet resultSet)
            throws SQLException {

        Account account = new Account();

        account.setAccountNumber(
                resultSet.getLong("account_number"));

        account.setCustomerId(
                resultSet.getInt("customer_id"));

        account.setAccountType(
                resultSet.getString("account_type"));

        account.setBalance(
                resultSet.getBigDecimal("balance"));

        account.setStatus(
                resultSet.getString("status"));

        account.setOpenDate(
                resultSet.getDate("open_date"));

        return account;
    }
}