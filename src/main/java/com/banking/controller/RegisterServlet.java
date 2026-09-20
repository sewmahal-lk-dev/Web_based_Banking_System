package com.banking.controller;

import com.banking.dao.CustomerDAO;
import com.banking.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private CustomerDAO customerDAO;

    @Override
    public void init() {

        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // ========================================
        // GET FORM VALUES
        // ========================================

        String name = clean(request.getParameter("name"));

        String email = clean(request.getParameter("email"))
                .toLowerCase();

        String phone = clean(request.getParameter("phone"));

        String dateOfBirth = clean(request.getParameter("dateOfBirth"));

        String accountType = clean(request.getParameter("accountType"));

        String address = clean(request.getParameter("address"));

        String city = clean(request.getParameter("city"));

        String postalCode = clean(request.getParameter("postalCode"));

        String password = request.getParameter("password");

        String confirmPassword = request.getParameter("confirmPassword");

        // ========================================
        // VALIDATION
        // ========================================

        if (name.isEmpty() ||
                email.isEmpty() ||
                phone.isEmpty() ||
                accountType.isEmpty() ||
                password == null ||
                confirmPassword == null) {

            showError(
                    request,
                    response,
                    "Please complete all required fields.");

            return;
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showError(
                    request,
                    response,
                    "Please enter a valid email address.");

            return;
        }

        if (name.length() > 100 || email.length() > 150 || !phone.matches("[+0-9 ()-]{7,20}") || address.length() > 255
                || city.length() > 100 || postalCode.length() > 20) {
            showError(request, response, "Check the length of your profile fields and enter a valid phone number.");
            return;
        }
        if (!dateOfBirth.isEmpty()) {
            try {
                if (java.time.LocalDate.parse(dateOfBirth).isAfter(java.time.LocalDate.now()))
                    throw new IllegalArgumentException();
            } catch (java.time.DateTimeException | IllegalArgumentException e) {
                showError(request, response, "Enter a valid date of birth that is not in the future.");
                return;
            }
        }
        if (password.length() < 8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {

            showError(
                    request,
                    response,
                    "Password must contain at least 8 characters and at most 72 UTF-8 bytes.");

            return;
        }

        if (!password.equals(confirmPassword)) {

            showError(
                    request,
                    response,
                    "Password and Confirm Password do not match.");

            return;
        }

        if (!accountType.equals("SAVINGS") &&
                !accountType.equals("CURRENT")) {

            showError(
                    request,
                    response,
                    "Please select a valid account type.");

            return;
        }

        try {

            // ========================================
            // CHECK EMAIL
            // ========================================

            if (customerDAO.emailExists(email)) {

                showError(
                        request,
                        response,
                        "An account already exists with this email address.");

                return;
            }

            // ========================================
            // CHECK PHONE
            // ========================================

            if (customerDAO.phoneExists(phone)) {

                showError(
                        request,
                        response,
                        "An account already exists with this phone number.");

                return;
            }

            // ========================================
            // HASH PASSWORD
            // ========================================

            String hashedPassword = PasswordUtil.hashPassword(password);

            // ========================================
            // CREATE CUSTOMER + ACCOUNT
            // ========================================

            int customerId = customerDAO.createCustomerAndAccount(
                    name,
                    address,
                    city,
                    postalCode,
                    phone,
                    email,
                    dateOfBirth,
                    hashedPassword,
                    accountType);

            // ========================================
            // SUCCESS
            // ========================================

            System.out.println(
                    "Customer registered successfully. ID: "
                            + customerId);

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.jsp?registered=1");

        } catch (IllegalArgumentException e) {

            showError(
                    request,
                    response,
                    "Invalid date or registration information.");

        } catch (SQLException e) {

            e.printStackTrace();

            showError(
                    request,
                    response,
                    "Registration failed due to a database error.");
        }
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message) throws ServletException, IOException {

        request.setAttribute(
                "error",
                message);

        request.getRequestDispatcher(
                "/register.jsp").forward(
                        request,
                        response);
    }

    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}
