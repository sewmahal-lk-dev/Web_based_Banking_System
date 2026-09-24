package com.banking.controller;

import com.banking.dao.TransferReceiptDAO;
import com.banking.util.Input;
import com.banking.util.TransferReceiptPdf;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/customer/transfer/receipt")
public class TransferReceiptServlet extends HttpServlet {
    private final TransferReceiptDAO dao=new TransferReceiptDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException {
        HttpSession session=req.getSession(false);
        if(session==null||session.getAttribute("userId")==null){res.sendRedirect(req.getContextPath()+"/login.jsp");return;}
        if(!"CUSTOMER".equals(session.getAttribute("role"))){res.sendError(403);return;}
        res.setHeader("Cache-Control","no-store");res.setHeader("X-Content-Type-Options","nosniff");
        try {
            String reference=Input.text(req.getParameter("reference"),100,"transaction reference");
            var receipt=dao.findCompleted(Input.id(session.getAttribute("userId").toString()),reference);
            // Do not disclose whether another customer's reference exists.
            if(receipt.isEmpty()){res.sendError(404,"Completed transfer receipt not found.");return;}
            byte[] pdf=TransferReceiptPdf.create(receipt.get());
            String filename=receipt.get().reference().replaceAll("[^A-Za-z0-9_-]","_");
            res.setContentType("application/pdf");
            res.setHeader("Content-Disposition","attachment; filename=\"LankaTrust-Transfer-Receipt-"+filename+".pdf\"");
            res.setContentLength(pdf.length);res.getOutputStream().write(pdf);
        }catch(IllegalArgumentException e){res.sendError(400,"Invalid transaction reference.");}
        catch(SQLException e){log("Transfer receipt lookup failed",e);res.sendError(503,"Receipt temporarily unavailable. Please try again.");}
    }
}
