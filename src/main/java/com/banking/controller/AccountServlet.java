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
import java.util.List;

@WebServlet("/customer/accounts")
public class AccountServlet extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request,
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

        if (role == null || !"CUSTOMER".equals(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        Object userIdObject = session.getAttribute("userId");

        int customerId;

        if (userIdObject instanceof Integer) {
            customerId = (Integer) userIdObject;

        } else if (userIdObject instanceof Long) {
            customerId = ((Long) userIdObject).intValue();

        } else {
            customerId = Integer.parseInt(userIdObject.toString());
        }

        List<Account> accounts = accountDAO.getAccountsByCustomerId(customerId);

        request.setAttribute("accounts", accounts);

        request.getRequestDispatcher(
                "/customer/accounts.jsp")
                .forward(request, response);
    }
}