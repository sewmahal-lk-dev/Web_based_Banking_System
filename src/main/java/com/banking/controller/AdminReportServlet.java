package com.banking.controller;

import com.banking.dao.ReportDAO;
import com.banking.model.AdminReport;
import com.banking.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.*;

@WebServlet(urlPatterns={"/admin/reports","/admin/reports/pdf"})
public class AdminReportServlet extends HttpServlet {
    private final ReportDAO dao=new ReportDAO();
    private boolean authorize(HttpServletRequest req,HttpServletResponse res)throws IOException {
        HttpSession session=req.getSession(false);
        if(session==null||session.getAttribute("userId")==null){res.sendRedirect(req.getContextPath()+"/login.jsp");return false;}
        if(!"SYSTEM_ADMIN".equals(session.getAttribute("role"))||!"EMPLOYEE".equals(session.getAttribute("userType"))){res.sendError(403);return false;}
        res.setHeader("Cache-Control","no-store");return true;
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {
        if(!authorize(req,res))return;
        AdminReport report=(AdminReport)req.getSession().getAttribute("adminReport");
        if(req.getServletPath().endsWith("/pdf")) {
            if(report==null||!report.token().equals(req.getParameter("report"))){res.sendError(409,"Generate the report again before downloading.");return;}
            byte[] pdf=AdminReportPdf.create(report);
            res.setContentType("application/pdf");res.setHeader("Content-Disposition","attachment; filename=\"LankaTrust-"+report.type()+"-Report.pdf\"");
            res.setContentLength(pdf.length);res.getOutputStream().write(pdf);return;
        }
        try {
            int page=req.getParameter("page")==null?1:Input.id(req.getParameter("page"));
            int pages=report==null?1:Math.max(1,(report.rows().size()+24)/25);
            if(page>pages)throw new IllegalArgumentException("Invalid report page.");
            req.setAttribute("report",report);req.setAttribute("pageNumber",page);req.setAttribute("pageCount",pages);
            req.setAttribute("totals",dao.totals());req.setAttribute("productTypes",dao.productTypes());
        }catch(IllegalArgumentException e){res.sendError(400,e.getMessage());return;}
        catch(SQLException e){log("Report page failed",e);res.sendError(503,"Reports are temporarily unavailable.");return;}
        req.getRequestDispatcher("/WEB-INF/admin/reports.jsp").forward(req,res);
    }
    private LocalDate date(String value) {
        try{return value==null||value.isBlank()?null:LocalDate.parse(value);}
        catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Enter a valid report date.");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {
        if(!authorize(req,res))return;
        if(req.getServletPath().endsWith("/pdf")){res.sendError(405);return;}
        try {
            ReportDAO.Filter filter=new ReportDAO.Filter(req.getParameter("type"),req.getParameter("status"),req.getParameter("role"),
                    req.getParameter("productType"),req.getParameter("action"),date(req.getParameter("from")),date(req.getParameter("to")));
            AdminReport report=dao.generate(Input.id(req.getSession().getAttribute("userId").toString()),
                    String.valueOf(req.getSession().getAttribute("userName")),filter);
            req.getSession().setAttribute("adminReport",report);req.getSession().setAttribute("adminReportFilter",filter);
            res.sendRedirect(req.getContextPath()+"/admin/reports");
        }catch(IllegalArgumentException e){req.setAttribute("reportError",e.getMessage());res.setStatus(400);doGet(req,res);}
        catch(SQLException e){log("Report generation failed",e);req.setAttribute("reportError","Unable to generate the report. Please try again.");res.setStatus(503);doGet(req,res);}
    }
}
