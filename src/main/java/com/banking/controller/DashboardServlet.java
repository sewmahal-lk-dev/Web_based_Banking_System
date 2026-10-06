package com.banking.controller;

import com.banking.dao.AccountDAO;
import com.banking.model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/customer/dashboard")
public class DashboardServlet extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        String role = (String) session.getAttribute("role");

        if (!"CUSTOMER".equals(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        Object userIdObject = session.getAttribute("userId");

        int customerId;

        try {

            if (userIdObject instanceof Integer) {

                customerId = (Integer) userIdObject;

            } else if (userIdObject instanceof Long) {

                customerId = ((Long) userIdObject).intValue();

            } else {

                customerId = Integer.parseInt(
                        userIdObject.toString());
            }

        } catch (Exception e) {

            session.invalidate();

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        Account account = accountDAO.getFirstAccountByCustomerId(
                customerId);
        // Read-only presentation data for the account overview; financial workflows are
        // unchanged.
        request.setAttribute("accounts", accountDAO.getAccountsByCustomerId(customerId));

        try {
            request.setAttribute("transactions", new com.banking.dao.TransactionDAO().history(customerId, 5, 0));
        } catch (java.sql.SQLException e) {
            log("Unable to load recent transactions", e);
            request.setAttribute("transactionError", true);
        }

        request.setAttribute(
                "account",
                account);

        request.setAttribute(
                "dashboardLoaded",
                true);

        request.getRequestDispatcher(
                "/customer/dashboard.jsp")
                .forward(request, response);
    }
}
