package com.banking.controller;

import com.banking.dao.NotificationDAO;
import com.banking.util.Input;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns={"/customer/notifications","/employee/notifications"})
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO dao=new NotificationDAO();
    private int user(HttpServletRequest req){return Input.id(req.getSession().getAttribute("userId").toString());}
    private boolean customer(HttpServletRequest req){return "CUSTOMER".equals(req.getSession().getAttribute("role"));}
    protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {
        int page=1;try{page=Math.max(1,Math.min(10000,Integer.parseInt(req.getParameter("page"))));}catch(RuntimeException ignored){}
        try {
            req.setAttribute("notificationPage",dao.list(user(req),customer(req),(page-1)*25,25));
            req.setAttribute("pageNumber",page);
            req.getRequestDispatcher("/WEB-INF/notifications.jsp").forward(req,res);
        }catch(SQLException e){log("Notification list failed",e);res.sendError(503,"Notifications are temporarily unavailable.");}
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {
        try {
            String action=Input.choice(req.getParameter("action"),"open","read-all");
            String target=req.getServletPath();
            if("read-all".equals(action))dao.markAll(user(req),customer(req));
            else {
                long id;try{id=Long.parseLong(req.getParameter("id"));}catch(RuntimeException e){throw new IllegalArgumentException("Invalid notification ID.");}
                target=dao.open(user(req),customer(req),id);
            }
            res.sendRedirect(req.getContextPath()+target);
        }catch(IllegalArgumentException e){res.sendError(404,"Notification not found.");}
        catch(SQLException e){log("Notification update failed",e);res.sendError(503,"Notifications are temporarily unavailable.");}
    }
}
