package com.banking.dao;

import com.banking.model.User;
import com.banking.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User findByEmail(String email) {

        String customerSql = "SELECT customer_id, name, email, password " +
                "FROM customer WHERE email = ? AND status = 'ACTIVE'";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(customerSql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new User(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            "CUSTOMER",
                            "CUSTOMER");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        String employeeSql = "SELECT employee_id, name, email, password, role " +
                "FROM employee WHERE email = ? AND status = 'ACTIVE'";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(employeeSql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new User(
                            rs.getInt("employee_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role"),
                            "EMPLOYEE");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}