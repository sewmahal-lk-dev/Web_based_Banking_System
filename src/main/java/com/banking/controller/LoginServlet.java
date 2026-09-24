package com.banking.controller;

import com.banking.dao.UserDAO;
import com.banking.model.User;
import com.banking.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

        private final UserDAO userDAO = new UserDAO();

        @Override
        protected void doPost(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                request.setCharacterEncoding("UTF-8");

                String email = request.getParameter("email");
                String password = request.getParameter("password");

                // ==========================================
                // BASIC VALIDATION
                // ==========================================

                if (email == null ||
                                password == null ||
                                email.trim().isEmpty() ||
                                password.trim().isEmpty()) {

                        request.setAttribute(
                                        "error",
                                        "Please enter your email and password.");

                        request.getRequestDispatcher("/login.jsp")
                                        .forward(request, response);

                        return;
                }

                email = email.trim().toLowerCase();

                // ==========================================
                // FIND USER
                // ==========================================

                User user;

                try {

                        user = userDAO.findByEmail(email);

                } catch (Exception e) {

                        e.printStackTrace();

                        request.setAttribute(
                                        "error",
                                        "Unable to connect to the banking system.");

                        request.getRequestDispatcher("/login.jsp")
                                        .forward(request, response);

                        return;
                }

                // ==========================================
                // USER NOT FOUND
                // ==========================================

                if (user == null) {

                        request.setAttribute(
                                        "error",
                                        "Invalid email or password.");

                        request.getRequestDispatcher("/login.jsp")
                                        .forward(request, response);

                        return;
                }

                // ==========================================
                // PASSWORD CHECK
                // ==========================================

                boolean correctPassword = PasswordUtil.checkPassword(
                                password,
                                user.getPassword());

                if (!correctPassword) {

                        request.setAttribute(
                                        "error",
                                        "Invalid email or password.");

                        request.getRequestDispatcher("/login.jsp")
                                        .forward(request, response);

                        return;
                }

                // ==========================================
                // REMOVE OLD SESSION
                // ==========================================

                HttpSession oldSession = request.getSession(false);

                if (oldSession != null) {
                        oldSession.invalidate();
                }

                // ==========================================
                // CREATE NEW SESSION
                // ==========================================

                HttpSession session = request.getSession(true);

                session.setAttribute(
                                "userId",
                                user.getId());

                session.setAttribute(
                                "userName",
                                user.getName());

                session.setAttribute(
                                "userEmail",
                                user.getEmail());

                session.setAttribute(
                                "role",
                                user.getRole());

                session.setAttribute(
                                "userType",
                                user.getUserType());

                session.setMaxInactiveInterval(
                                30 * 60);

                // ==========================================
                // ROLE REDIRECTION
                // ==========================================

                String role = user.getRole();

                if (role == null) {

                        session.invalidate();

                        request.setAttribute(
                                        "error",
                                        "No banking role is assigned to this account.");

                        request.getRequestDispatcher("/login.jsp")
                                        .forward(request, response);

                        return;
                }

                switch (role) {

                        case "CUSTOMER":

                                /*
                                 * IMPORTANT:
                                 * Customer must go through DashboardServlet.
                                 * DashboardServlet loads the real bank account
                                 * information from MySQL.
                                 */
                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/customer/dashboard");

                                return;

                        case "LOAN_OFFICER":

                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/loan/dashboard.jsp");

                                return;

                        case "CARD_SERVICES_OFFICER":

                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/card/dashboard.jsp");

                                return;

                        case "INVESTMENT_OFFICER":

                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/investment/dashboard.jsp");

                                return;

                        case "CUSTOMER_SERVICE_OFFICER":

                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/service/dashboard.jsp");

                                return;

                        case "COMPLIANCE_RISK_OFFICER":

                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/compliance/dashboard.jsp");

                                return;

                        case "SYSTEM_ADMIN":

                                response.sendRedirect(
                                                request.getContextPath()
                                                                + "/admin/dashboard.jsp");

                                return;

                        default:

                                session.invalidate();

                                request.setAttribute(
                                                "error",
                                                "Invalid banking role.");

                                request.getRequestDispatcher("/login.jsp")
                                                .forward(request, response);
                }
        }
}