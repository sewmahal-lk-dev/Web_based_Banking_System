package com.banking.controller;

import com.banking.dao.AccountDAO;
import com.banking.dao.PaymentDAO;
import com.banking.model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/customer/payments")
public class PaymentServlet extends HttpServlet {

        private final PaymentDAO paymentDAO = new PaymentDAO();

        private final AccountDAO accountDAO = new AccountDAO();

        @Override
        protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                HttpSession session = request.getSession(false);

                if (!isCustomerLoggedIn(session)) {

                        response.sendRedirect(
                                        request.getContextPath()
                                                        + "/login.jsp");

                        return;
                }

                Object flash = session.getAttribute("flash");
                session.removeAttribute("flash");
                request.setAttribute("success", flash);
                int customerId = getCustomerId(session);

                Account account = accountDAO
                                .getFirstAccountByCustomerId(
                                                customerId);

                request.setAttribute(
                                "account",
                                account);

                request.setAttribute("error",
                                session.getAttribute("paymentError") != null ? session.getAttribute("paymentError")
                                                : request.getAttribute("error"));
                session.removeAttribute("paymentError");
                if (request.getAttribute("error") != null)
                        request.removeAttribute("success");
                try {
                        request.setAttribute("scheduledPayments",
                                        new com.banking.dao.ScheduledPaymentDAO().list(customerId));
                } catch (java.sql.SQLException e) {
                        log("Unable to load scheduled payments", e);
                        request.setAttribute("scheduleError", true);
                }

                request.getRequestDispatcher(
                                "/customer/payments.jsp")
                                .forward(request, response);
        }

        @Override
        protected void doPost(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                request.setCharacterEncoding("UTF-8");

                HttpSession session = request.getSession(false);

                if (!isCustomerLoggedIn(session)) {

                        response.sendRedirect(
                                        request.getContextPath()
                                                        + "/login.jsp");

                        return;
                }

                int customerId = getCustomerId(session);

                String recipient = clean(
                                request.getParameter(
                                                "recipient"));

                String amountValue = clean(
                                request.getParameter(
                                                "amount"));

                try {

                        if (recipient.isEmpty() ||
                                        amountValue.isEmpty()) {

                                throw new IllegalArgumentException(
                                                "Please enter the service provider and amount.");
                        }

                        BigDecimal amount = new BigDecimal(
                                        amountValue);

                        String reference = paymentDAO.makeBillPayment(
                                        customerId,
                                        recipient,
                                        request.getParameter("billReference"),
                                        amount);

                        session.setAttribute(
                                        "flash",
                                        "Payment completed successfully. Reference: "
                                                        + reference);
                        response.sendRedirect(request.getContextPath() + request.getServletPath());
                        return;

                } catch (NumberFormatException e) {

                        request.setAttribute(
                                        "error",
                                        "Please enter a valid payment amount.");

                } catch (IllegalArgumentException e) {

                        request.setAttribute(
                                        "error",
                                        e.getMessage());

                } catch (Exception e) {

                        e.printStackTrace();

                        request.setAttribute(
                                        "error",
                                        "Payment failed due to a database error.");
                }

                Account account = accountDAO
                                .getFirstAccountByCustomerId(
                                                customerId);

                request.setAttribute(
                                "account",
                                account);

                request.setAttribute("error",
                                session.getAttribute("paymentError") != null ? session.getAttribute("paymentError")
                                                : request.getAttribute("error"));
                session.removeAttribute("paymentError");
                if (request.getAttribute("error") != null)
                        request.removeAttribute("success");
                try {
                        request.setAttribute("scheduledPayments",
                                        new com.banking.dao.ScheduledPaymentDAO().list(customerId));
                } catch (java.sql.SQLException e) {
                        log("Unable to load scheduled payments", e);
                        request.setAttribute("scheduleError", true);
                }

                request.getRequestDispatcher(
                                "/customer/payments.jsp")
                                .forward(request, response);
        }

        private boolean isCustomerLoggedIn(
                        HttpSession session) {

                return session != null
                                && session.getAttribute("userId") != null
                                && "CUSTOMER".equals(
                                                session.getAttribute("role"));
        }

        private int getCustomerId(
                        HttpSession session) {

                Object userId = session.getAttribute("userId");

                if (userId instanceof Integer) {
                        return (Integer) userId;
                }

                if (userId instanceof Long) {
                        return ((Long) userId).intValue();
                }

                return Integer.parseInt(
                                userId.toString());
        }

        private String clean(String value) {

                if (value == null) {
                        return "";
                }

                return value.trim();
        }
}