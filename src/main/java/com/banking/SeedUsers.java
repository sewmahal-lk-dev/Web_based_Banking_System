package com.banking;

import com.banking.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SeedUsers {

        public static void main(String[] args) {

                try (Connection con = DBConnection.getConnection()) {

                        addCustomer(
                                        con,
                                        "Demo Customer",
                                        "customer@bank.com",
                                        "Customer@123");

                        addEmployee(
                                        con,
                                        "Loan Officer",
                                        "loan@bank.com",
                                        "Officer@123",
                                        "LOAN_OFFICER");

                        addEmployee(
                                        con,
                                        "Card Services Officer",
                                        "card@bank.com",
                                        "Officer@123",
                                        "CARD_SERVICES_OFFICER");

                        addEmployee(
                                        con,
                                        "Investment Officer",
                                        "investment@bank.com",
                                        "Officer@123",
                                        "INVESTMENT_OFFICER");

                        addEmployee(
                                        con,
                                        "Customer Service Officer",
                                        "support@bank.com",
                                        "Officer@123",
                                        "CUSTOMER_SERVICE_OFFICER");

                        addEmployee(
                                        con,
                                        "Compliance & Risk Officer",
                                        "compliance@bank.com",
                                        "Officer@123",
                                        "COMPLIANCE_RISK_OFFICER");

                        addEmployee(
                                        con,
                                        "System Administrator",
                                        "admin@bank.com",
                                        "Admin@123",
                                        "SYSTEM_ADMIN");

                        System.out.println("==================================");
                        System.out.println("TEST USERS CREATED SUCCESSFULLY!");
                        System.out.println("==================================");

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private static void addCustomer(
                        Connection con,
                        String name,
                        String email,
                        String password) throws Exception {

                String sql = "INSERT INTO customer " +
                                "(name, email, password, status) " +
                                "VALUES (?, ?, ?, 'ACTIVE')";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setString(2, email);
                        ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt()));
                        ps.executeUpdate();
                }
        }

        private static void addEmployee(
                        Connection con,
                        String name,
                        String email,
                        String password,
                        String role) throws Exception {

                String sql = "INSERT INTO employee " +
                                "(name, email, password, role, status) " +
                                "VALUES (?, ?, ?, ?, 'ACTIVE')";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setString(2, email);
                        ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt()));
                        ps.setString(4, role);
                        ps.executeUpdate();
                }
        }
}