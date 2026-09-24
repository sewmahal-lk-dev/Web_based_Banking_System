package com.banking.controller;

import com.banking.dao.AdminDAO;
import com.banking.dao.CardDAO;
import com.banking.dao.CashTransactionDAO;
import com.banking.dao.EmployeeDAO;
import com.banking.dao.TicketDAO;
import com.banking.dao.InvestmentDAO;
import com.banking.dao.LoanDAO;
import com.banking.dao.ServiceRequestDAO;
import com.banking.util.Input;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet(urlPatterns = {
                "/employee/dashboard",
                "/loan/dashboard.jsp",
                "/card/dashboard.jsp",
                "/investment/dashboard.jsp",
                "/service/dashboard.jsp",
                "/compliance/dashboard.jsp",
                "/admin/dashboard.jsp"
})
public class EmployeeServlet extends HttpServlet {

        private final EmployeeDAO dao = new EmployeeDAO();

        @Override
        protected void doGet(
                        HttpServletRequest req,
                        HttpServletResponse res) throws ServletException, IOException {

                String role = (String) req.getSession().getAttribute("role");

                try {

                        if(TicketDAO.ROLES.contains(role))req.setAttribute("supportTickets",new TicketDAO().list(Input.id(req.getSession().getAttribute("userId").toString())));

                        req.setAttribute(
                                        "groups",
                                        dao.dashboard(
                                                        role,
                                                        req.getParameter("q")));

                } catch (SQLException e) {

                        log("Employee dashboard failed", e);

                        req.setAttribute(
                                        "error",
                                        "Unable to load bank records. Please try again.");
                }

                if ("SYSTEM_ADMIN".equals(role)) {
                        try {
                                req.setAttribute("adminRecentActivity",new AdminDAO().recentActivity(
                                                Input.id(req.getSession().getAttribute("userId").toString())));
                        } catch (SQLException e) {
                                log("Recent administrative activity failed",e);
                                req.setAttribute("adminActivityUnavailable",true);
                        }
                }

                if (req.getAttribute("error") == null) {

                        req.setAttribute(
                                        "success",
                                        req.getSession().getAttribute("flash"));
                }

                req.getSession().removeAttribute("flash");

                if ("CARD_SERVICES_OFFICER".equals(role) && req.getParameter("findCard") != null) {
                        try {
                                req.setAttribute("closureCards", new CardDAO().findForClosure(
                                                req.getParameter("cardId"), req.getParameter("cardNumber")));
                        } catch (IllegalArgumentException e) {
                                req.setAttribute("cardLookupError", e.getMessage());
                        } catch (SQLException e) {
                                log("Card lookup failed", e);
                                req.setAttribute("cardLookupError", "Unable to find cards. Please try again.");
                        }
                }

                req.getRequestDispatcher(
                                "/WEB-INF/employee/dashboard.jsp").forward(req, res);
        }

        @Override
        protected void doPost(
                        HttpServletRequest req,
                        HttpServletResponse res) throws ServletException, IOException {

                try {

                        int employee = Input.id(
                                        req.getSession()
                                                        .getAttribute("userId")
                                                        .toString());

                        String role = (String) req.getSession()
                                        .getAttribute("role");

                        String action = Input.text(
                                        req.getParameter("action"),
                                        50,
                                        "action");

                        String flashMessage;

                        switch (action) {
                                case "ticket-assign" -> {
                                        new TicketDAO().assign(employee,Input.id(req.getParameter("id")),req.getParameter("assignedRole"));
                                        flashMessage="Ticket assigned to "+TicketDAO.department(req.getParameter("assignedRole"))+" successfully.";
                                }
                                case "ticket-response", "ticket-status", "ticket-reply" -> {
                                        new TicketDAO().update(employee,Input.id(req.getParameter("id")),action,req.getParameter("status"),req.getParameter("reason"));
                                        flashMessage="Support ticket updated successfully.";
                                }


                                /*
                                 * =========================
                                 * LOAN MANAGEMENT
                                 * =========================
                                 */

                                case "loan-approve",
                                                "loan-reject" -> {

                                        new LoanDAO().decision(
                                                        employee,
                                                        Input.id(req.getParameter("id")),
                                                        "loan-approve".equals(action),
                                                        req.getParameter("reason"));

                                        flashMessage = "Loan operation completed successfully.";
                                }

                                /*
                                 * =========================
                                 * CARD MANAGEMENT
                                 * =========================
                                 */

                                case "card-approve",
                                                "card-reject",
                                                "card-block",
                                                "card-unblock",
                                                "card-close" -> {

                                        new CardDAO().process(
                                                        employee,
                                                        Input.id(req.getParameter("id")),
                                                        action.substring(5),
                                                        req.getParameter("reason"));

                                        flashMessage = "Card operation completed successfully.";
                                }

                                /*
                                 * =========================
                                 * INVESTMENT MANAGEMENT
                                 * =========================
                                 */

                                case "investment-approve",
                                                "investment-reject" -> {

                                        new InvestmentDAO().process(
                                                        employee,
                                                        Input.id(req.getParameter("id")),
                                                        "investment-approve".equals(action),
                                                        req.getParameter("reason"));

                                        flashMessage = "Investment decision completed successfully.";
                                }

                                case "investment-mature" -> {

                                        new InvestmentDAO().mature(
                                                        employee,
                                                        Input.id(req.getParameter("id")));

                                        flashMessage = "Investment maturity processed successfully.";
                                }

                                /*
                                 * =========================
                                 * SERVICE REQUEST MANAGEMENT
                                 * =========================
                                 */

                                case "request-review" -> {

                                        new ServiceRequestDAO().process(
                                                        employee,
                                                        Input.id(req.getParameter("id")),
                                                        req.getParameter("status"),
                                                        req.getParameter("reason"));

                                        flashMessage = "Service request updated successfully.";
                                }

                                /*
                                 * =========================
                                 * CASH DEPOSIT
                                 * CUSTOMER SERVICE OFFICER
                                 * =========================
                                 */

                                case "cash-deposit" -> {

                                        requireCustomerServiceOfficer(role);

                                        long accountNumber = parseAccountNumber(
                                                        req.getParameter("accountNumber"));

                                        BigDecimal amount = parseAmount(
                                                        req.getParameter("amount"));

                                        String reference = new CashTransactionDAO().deposit(
                                                        employee,
                                                        accountNumber,
                                                        amount,
                                                        req.getParameter("note"));

                                        flashMessage = "Cash deposit completed successfully. " +
                                                        "Reference: " + reference;
                                }

                                /*
                                 * =========================
                                 * CASH WITHDRAWAL
                                 * CUSTOMER SERVICE OFFICER
                                 * =========================
                                 */

                                case "cash-withdrawal" -> {

                                        requireCustomerServiceOfficer(role);

                                        long accountNumber = parseAccountNumber(
                                                        req.getParameter("accountNumber"));

                                        BigDecimal amount = parseAmount(
                                                        req.getParameter("amount"));

                                        String reference = new CashTransactionDAO().withdraw(
                                                        employee,
                                                        accountNumber,
                                                        amount,
                                                        req.getParameter("note"));

                                        flashMessage = "Cash withdrawal completed successfully. " +
                                                        "Reference: " + reference;
                                }

                                /*
                                 * =========================
                                 * ADMIN - EMPLOYEE CRUD
                                 * =========================
                                 */

                                case "employee-save" -> {

                                        new AdminDAO().employee(
                                                        employee,
                                                        optionalId(req.getParameter("id")),
                                                        req.getParameter("name"),
                                                        req.getParameter("email"),
                                                        req.getParameter("phone"),
                                                        req.getParameter("role"),
                                                        req.getParameter("status"),
                                                        req.getParameter("password"));

                                        flashMessage = "Employee record saved successfully.";
                                }

                                /*
                                 * =========================
                                 * ADMIN - PRODUCT CRUD
                                 * =========================
                                 */

                                case "product-save" -> {

                                        new AdminDAO().product(
                                                        employee,
                                                        optionalId(req.getParameter("id")),
                                                        req.getParameter("name"),
                                                        req.getParameter("type"),
                                                        req.getParameter("description"),
                                                        req.getParameter("status"));

                                        flashMessage = "Banking product saved successfully.";
                                }

                                /*
                                 * =========================
                                 * ADMIN - CUSTOMER STATUS
                                 * =========================
                                 */

                                case "customer-status" -> {

                                        new AdminDAO().customerStatus(
                                                        employee,
                                                        Input.id(req.getParameter("id")),
                                                        req.getParameter("status"));

                                        flashMessage = "Customer status updated successfully.";
                                }

                                /*
                                 * =========================
                                 * EXISTING ROLE ACTIONS
                                 * =========================
                                 */

                                default -> {

                                        dao.act(
                                                        employee,
                                                        role,
                                                        action,
                                                        req.getParameter("id"),
                                                        req.getParameter("status"),
                                                        req.getParameter("reason"));

                                        flashMessage = "Operation completed and audit record saved.";
                                }
                        }

                        req.getSession().setAttribute(
                                        "flash",
                                        flashMessage);

                        res.sendRedirect(
                                        req.getContextPath()
                                                        + req.getServletPath()
                                                        + (action.startsWith("ticket-")?"?ticket="+Input.id(req.getParameter("id"))+"#support-ticket-"+Input.id(req.getParameter("id")):""));

                } catch (IllegalArgumentException | SecurityException e) {

                        req.setAttribute(
                                        "error",
                                        e.getMessage());

                        doGet(req, res);

                } catch (SQLException e) {

                        log(
                                        "Employee action failed",
                                        e);

                        req.setAttribute(
                                        "error",
                                        "Operation failed; no changes were committed.");

                        doGet(req, res);
                }
        }

        /*
         * Only Customer Service Officers are allowed
         * to access teller-style cash transactions.
         */
        private void requireCustomerServiceOfficer(
                        String role) {

                if (!"CUSTOMER_SERVICE_OFFICER".equals(role)) {

                        throw new SecurityException(
                                        "Only Customer Service Officers can perform cash transactions.");
                }
        }

        /*
         * Parses and validates account number.
         */
        private long parseAccountNumber(
                        String value) {

                if (value == null || value.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Account number is required.");
                }

                try {

                        long accountNumber = Long.parseLong(value.trim());

                        if (accountNumber <= 0) {

                                throw new IllegalArgumentException(
                                                "Invalid account number.");
                        }

                        return accountNumber;

                } catch (NumberFormatException e) {

                        throw new IllegalArgumentException(
                                        "Invalid account number.");
                }
        }

        /*
         * Parses the financial amount.
         *
         * The DAO performs the final amount validation
         * before committing the transaction.
         */
        private BigDecimal parseAmount(
                        String value) {

                if (value == null || value.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Amount is required.");
                }

                try {

                        return new BigDecimal(
                                        value.trim());

                } catch (NumberFormatException e) {

                        throw new IllegalArgumentException(
                                        "Invalid transaction amount.");
                }
        }

        /*
         * Used by Admin CRUD.
         * Empty ID means CREATE.
         * Existing ID means UPDATE.
         */
        private Integer optionalId(
                        String value) {

                if (value == null || value.trim().isEmpty()) {
                        return null;
                }

                return Input.id(
                                value.trim());
        }
}
