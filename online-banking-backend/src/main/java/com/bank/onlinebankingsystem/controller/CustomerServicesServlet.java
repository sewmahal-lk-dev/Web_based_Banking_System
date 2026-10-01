package com.banking.controller;

import com.banking.dao.*;
import com.banking.util.Input;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns={"/customer/cards","/customer/loans","/customer/investments","/customer/requests","/customer/tickets","/customer/settings","/customer/transactions","/customer/products"})
public class CustomerServicesServlet extends HttpServlet {
    private final CustomerServicesDAO dao=new CustomerServicesDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        int id=Input.id(req.getSession().getAttribute("userId").toString());
        String section=req.getServletPath().substring("/customer/".length());
        req.setAttribute("section",section);
        Object flash=req.getSession().getAttribute("flash");req.getSession().removeAttribute("flash");req.setAttribute("success",req.getAttribute("error")==null?flash:null);
        try {
            req.setAttribute("accounts",new AccountDAO().getAccountsByCustomerId(id));
            if("transactions".equals(section)) {
                int page=1;try{page=Math.max(1,Math.min(10000,Integer.parseInt(req.getParameter("page"))));}catch(RuntimeException ignored){}
                req.setAttribute("pageNumber",page);req.setAttribute("transactions",new TransactionDAO().history(id,25,(page-1)*25));
            } else {
                req.setAttribute("records",dao.list(id,section));
                if("loans".equals(section))req.setAttribute("repayments",dao.list(id,"repayments"));
                if("requests".equals(section))req.setAttribute("tickets",dao.list(id,"tickets"));
                if("tickets".equals(section))req.setAttribute("tickets",req.getAttribute("records"));
            }
        }catch(SQLException e){log("Customer data could not be loaded",e);req.setAttribute("error","Unable to load your information. Please try again.");req.setAttribute("transactionError",true);}
        req.getRequestDispatcher("/WEB-INF/customer/services.jsp").forward(req,res);
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        int id=Input.id(req.getSession().getAttribute("userId").toString());
        String section=req.getServletPath().substring("/customer/".length());
        try {
            String action=req.getParameter("action");
            switch(section) {
                case "loans" -> {
                    LoanDAO loans=new LoanDAO();
                    switch(Input.choice(action,"apply","update","cancel","accept","repay")) {
                        case "apply" -> loans.apply(id,req.getParameter("type"),req.getParameter("amount"),req.getParameter("months"));
                        case "update" -> loans.update(id,Input.id(req.getParameter("id")),req.getParameter("amount"),req.getParameter("months"));
                        case "cancel" -> loans.cancel(id,Input.id(req.getParameter("id")));
                        case "accept" -> loans.accept(id,Input.id(req.getParameter("id")));
                        case "repay" -> new LoanRepaymentDAO().pay(id,Input.id(req.getParameter("id")));
                    }
                }
                case "investments" -> {
                    InvestmentDAO investments=new InvestmentDAO();
                    switch(Input.choice(action,"apply","update","cancel","withdraw")) {
                        case "apply" -> investments.apply(id,req.getParameter("type"),req.getParameter("amount"),req.getParameter("months"));
                        case "update" -> investments.update(id,Input.id(req.getParameter("id")),req.getParameter("amount"),req.getParameter("months"));
                        case "cancel" -> investments.cancel(id,Input.id(req.getParameter("id")));
                        case "withdraw" -> investments.withdraw(id,Input.id(req.getParameter("id")));
                    }
                }
                case "cards" -> {
                    if("apply".equals(action))new CardDAO().apply(id,req.getParameter("type"));
                    else new CardDAO().customerAction(id,Input.id(req.getParameter("id")),action,req.getParameter("limit"));
                }
                case "tickets" -> {
                    switch(Input.choice(action,"ticket","ticket-update","ticket-close","ticket-reply")) {
                        case "ticket-reply" -> dao.reply(id,Input.id(req.getParameter("id")),req.getParameter("reply"));
                        case "ticket" -> dao.ticket(id,req.getParameter("type"),req.getParameter("subject"),req.getParameter("description"));
                        case "ticket-update","ticket-close" -> new ServiceRequestDAO().ticketUpdate(id,Input.id(req.getParameter("id")),req.getParameter("subject"),req.getParameter("description"),"ticket-close".equals(action));
                    }
                }
                case "requests" -> {
                    switch(Input.choice(action,"request","ticket","request-update","request-cancel","ticket-update","ticket-close","ticket-reply")) {
                        case "request" -> new ServiceRequestDAO().create(id,req.getParameter("type"),req.getParameter("description"),req.getParameter("account"),req.getParameter("accountType"),req.getParameter("email"));
                        case "ticket-reply" -> dao.reply(id,Input.id(req.getParameter("id")),req.getParameter("reply"));
                        case "ticket" -> dao.ticket(id,req.getParameter("type"),req.getParameter("subject"),req.getParameter("description"));
                        case "request-update","request-cancel" -> new ServiceRequestDAO().update(id,Input.id(req.getParameter("id")),req.getParameter("description"),"request-cancel".equals(action));
                        case "ticket-update","ticket-close" -> new ServiceRequestDAO().ticketUpdate(id,Input.id(req.getParameter("id")),req.getParameter("subject"),req.getParameter("description"),"ticket-close".equals(action));
                    }
                }
                case "settings" -> {
                    if("deactivate".equals(action)) {new AccountWorkflowDAO().deactivate(id,req.getParameter("currentPassword"));req.getSession().invalidate();res.sendRedirect(req.getContextPath()+"/login.jsp?deactivated=1");return;}
                    if("password".equals(action)) {
                        dao.password(id,req.getParameter("currentPassword"),req.getParameter("newPassword"),req.getParameter("confirmPassword"));
                        req.getSession().invalidate();res.sendRedirect(req.getContextPath()+"/login.jsp?passwordChanged=1");return;
                    }
                    Input.choice(action,"profile");dao.profile(id,req.getParameter("name"),req.getParameter("phone"),req.getParameter("address"),req.getParameter("city"),req.getParameter("postalCode"));
                    req.getSession().setAttribute("userName",req.getParameter("name").trim());
                }
                default -> {res.sendError(405);return;}
            }
            req.getSession().setAttribute("flash","Your changes were saved successfully.");
            res.sendRedirect(req.getContextPath()+req.getServletPath());
        }catch(IllegalArgumentException e){req.setAttribute("error",e.getMessage());doGet(req,res);}
        catch(SQLException e){log("Customer operation failed",e);req.setAttribute("error","Unable to save changes. Please check your details and try again.");doGet(req,res);}
    }
}
