package com.banking.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@WebFilter(urlPatterns={"/customer/*", "/employee/*", "/loan/*", "/card/*", "/investment/*", "/service/*", "/compliance/*", "/admin/*"})
public class AccessFilter implements Filter {
    private static final Map<String,String> ROLES = Map.of("loan","LOAN_OFFICER","card","CARD_SERVICES_OFFICER","investment","INVESTMENT_OFFICER","service","CUSTOMER_SERVICE_OFFICER","compliance","COMPLIANCE_RISK_OFFICER","admin","SYSTEM_ADMIN");
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)req;
        HttpServletResponse response=(HttpServletResponse)res;
        HttpSession session=request.getSession(false);
        if(session==null || session.getAttribute("userId")==null) { response.sendRedirect(request.getContextPath()+"/login.jsp"); return; }
        String path=request.getServletPath();
        String section=path.split("/")[1];
        String role=String.valueOf(session.getAttribute("role"));
        boolean allowed="customer".equals(section) ? "CUSTOMER".equals(role) : "EMPLOYEE".equals(session.getAttribute("userType")) && ("employee".equals(section) ? ROLES.containsValue(role) : role.equals(ROLES.get(section)));
        if(!allowed) { response.sendError(403); return; }
        try {
            if(!new com.banking.dao.SessionDAO().active(com.banking.util.Input.id(session.getAttribute("userId").toString()),role)) {
                session.invalidate();response.sendRedirect(request.getContextPath()+"/login.jsp");return;
            }
        } catch(java.sql.SQLException e) { request.getServletContext().log("Session verification failed",e);response.sendError(503,"Banking service temporarily unavailable.");return; }
        catch(IllegalArgumentException e) { session.invalidate();response.sendRedirect(request.getContextPath()+"/login.jsp");return; }
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control","no-store");
        response.setHeader("X-Content-Type-Options","nosniff");
        response.setHeader("X-Frame-Options","DENY");
        synchronized(session) { if(session.getAttribute("csrf")==null) session.setAttribute("csrf",UUID.randomUUID().toString()); }
        if("POST".equals(request.getMethod()) && !session.getAttribute("csrf").equals(request.getParameter("csrf"))) { response.sendError(403,"Session token missing or expired. Reload the form."); return; }
        if("customer".equals(section) && path.endsWith(".jsp")) { response.sendRedirect(request.getContextPath()+path.substring(0,path.length()-4)); return; }
        if("GET".equals(request.getMethod())||"POST".equals(request.getMethod())) {
            try {
                var notifications=new com.banking.dao.NotificationDAO();
                int user=com.banking.util.Input.id(session.getAttribute("userId").toString());
                boolean customer="CUSTOMER".equals(role);
                request.setAttribute("notificationUnread",notifications.unread(user,customer));
                request.setAttribute("notificationItems",notifications.list(user,customer,0,8));
            }catch(java.sql.SQLException e){request.getServletContext().log("Notification bell failed",e);request.setAttribute("notificationUnavailable",true);}
        }
        chain.doFilter(req,res);
    }
}
