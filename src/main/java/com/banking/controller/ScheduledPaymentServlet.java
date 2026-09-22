package com.banking.controller;

import com.banking.dao.ScheduledPaymentDAO;
import com.banking.util.Input;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/customer/payment-plans")
public class ScheduledPaymentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException {res.sendRedirect(req.getContextPath()+"/customer/payments");}
    protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException {
        int customer=Input.id(req.getSession().getAttribute("userId").toString());ScheduledPaymentDAO dao=new ScheduledPaymentDAO();
        try {
            switch(Input.choice(req.getParameter("action"),"schedule","update","cancel","execute")){
                case "schedule" -> dao.save(customer,null,req.getParameter("recipient"),req.getParameter("billReference"),req.getParameter("amount"),req.getParameter("date"));
                case "update" -> dao.save(customer,Input.longId(req.getParameter("id")),req.getParameter("recipient"),req.getParameter("billReference"),req.getParameter("amount"),req.getParameter("date"));
                case "cancel" -> dao.cancel(customer,Input.longId(req.getParameter("id")));
                case "execute" -> dao.execute(customer,Input.longId(req.getParameter("id")));
            }
            req.getSession().setAttribute("flash","Scheduled payment operation completed.");
        }catch(IllegalArgumentException e){req.getSession().setAttribute("paymentError",e.getMessage());}
        catch(SQLException e){log("Scheduled payment failed",e);req.getSession().setAttribute("paymentError","Unable to process the payment. No debit was committed.");}
        res.sendRedirect(req.getContextPath()+"/customer/payments");
    }
}
