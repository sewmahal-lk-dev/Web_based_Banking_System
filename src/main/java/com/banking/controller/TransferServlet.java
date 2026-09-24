package com.banking.controller;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransferDAO;
import com.banking.model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/customer/transfer")
public class TransferServlet extends HttpServlet {

        private final TransferDAO transferDAO = new TransferDAO();

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
                String receiptReference = request.getParameter("receipt");
                if (receiptReference != null) {
                        try {
                                var receipt = new com.banking.dao.TransferReceiptDAO().findCompleted(customerId,
                                                com.banking.util.Input.text(receiptReference, 100, "transaction reference"));
                                if (receipt.isPresent()) {
                                        request.setAttribute("transferReceipt", receipt.get());
                                        request.setAttribute("success", "Transfer completed successfully. Reference: " + receipt.get().reference());
                                }
                        } catch (java.sql.SQLException e) {
                                log("Transfer receipt confirmation lookup failed", e);
                                request.setAttribute("receiptUnavailable", true);
                        } catch (IllegalArgumentException ignored) {
                                // Invalid display references cannot authorize a receipt.
                        }
                }

                Account account = accountDAO
                                .getFirstAccountByCustomerId(
                                                customerId);

                request.setAttribute(
                                "account",
                                account);

                request.getRequestDispatcher(
                                "/customer/transfer.jsp")
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

                String receiverValue = clean(
                                request.getParameter(
                                                "receiverAccount"));

                String amountValue = clean(
                                request.getParameter(
                                                "amount"));

                try {

                        if (receiverValue.isEmpty() ||
                                        amountValue.isEmpty()) {

                                throw new IllegalArgumentException(
                                                "Please enter the receiver account number and amount.");
                        }

                        long receiverAccountNumber = Long.parseLong(receiverValue);

                        BigDecimal amount = new BigDecimal(amountValue);

                        String reference = transferDAO.transferMoney(
                                        customerId,
                                        receiverAccountNumber,
                                        amount);

                        session.setAttribute(
                                        "flash",
                                        "Transfer completed successfully. Reference: "
                                                        + reference);
                        response.sendRedirect(request.getContextPath() + request.getServletPath() + "?receipt="
                                        + java.net.URLEncoder.encode(reference, java.nio.charset.StandardCharsets.UTF_8));
                        return;

                } catch (NumberFormatException e) {

                        request.setAttribute(
                                        "error",
                                        "Please enter a valid account number and amount.");

                } catch (IllegalArgumentException e) {

                        request.setAttribute(
                                        "error",
                                        e.getMessage());

                } catch (Exception e) {

                        e.printStackTrace();

                        request.setAttribute(
                                        "error",
                                        "Transfer failed due to a database error.");
                }

                Account account = accountDAO
                                .getFirstAccountByCustomerId(
                                                customerId);

                request.setAttribute(
                                "account",
                                account);

                request.getRequestDispatcher(
                                "/customer/transfer.jsp")
                                .forward(request, response);
        }

        private boolean isCustomerLoggedIn(
                        HttpSession session) {

                return session != null &&
                                session.getAttribute("userId") != null &&
                                "CUSTOMER".equals(
                                                session.getAttribute("role"));
        }

        private int getCustomerId(
                        HttpSession session) {

                Object userId = session.getAttribute("userId");

                if (userId instanceof Integer) {

                        return (Integer) userId;
                }

                if (userId instanceof Long) {

                        return ((Long) userId)
                                        .intValue();
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