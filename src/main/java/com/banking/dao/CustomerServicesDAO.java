package com.banking.dao;

import com.banking.util.DBConnection;
import com.banking.util.Input;
import com.banking.util.PasswordUtil;
import java.sql.*;
import java.util.*;

public class CustomerServicesDAO {
    public List<Map<String,Object>> list(int customerId,String section) throws SQLException {
        String sql=switch(section) {
            case "cards" -> "SELECT c.card_id, CONCAT('**** ',RIGHT(c.card_number,4)) AS card_number,c.card_type,c.expiry_date,c.status,c.daily_limit,c.decision_note,cc.credit_limit,cc.available_credit,dc.daily_withdrawal_limit FROM card c JOIN account a ON a.account_number=c.account_number LEFT JOIN credit_card cc ON cc.card_id=c.card_id LEFT JOIN debit_card dc ON dc.card_id=c.card_id WHERE a.customer_id=? ORDER BY c.card_id DESC";
            case "loans" -> "SELECT loan_id,loan_type,amount,interest_rate,term_months,application_date,status,rejection_reason,account_number FROM loan WHERE customer_id=? ORDER BY loan_id DESC";
            case "repayments" -> "SELECT r.repayment_id,r.loan_id,r.amount,r.due_date,r.paid_date,r.status FROM loan_repayment r JOIN loan l ON l.loan_id=r.loan_id WHERE l.customer_id=? ORDER BY r.due_date,r.repayment_id";
            case "investments" -> "SELECT investment_id,investment_type,amount,interest_rate,start_date,maturity_date,status,term_months,payout_amount FROM investment WHERE customer_id=? ORDER BY investment_id DESC";
            case "requests" -> "SELECT request_id,request_type,description,status,rejection_reason,response,date_submitted,account_number,requested_account_type,requested_email FROM service_request WHERE customer_id=? ORDER BY request_id DESC";
            case "tickets" -> "SELECT t.*,e.name AS assigned_officer FROM ticket t LEFT JOIN employee e ON e.employee_id=t.assigned_employee_id WHERE t.customer_id=? ORDER BY t.ticket_id DESC";
            case "products" -> "SELECT product_id,product_name,product_type,description FROM banking_product WHERE status='ACTIVE' AND ? > 0 ORDER BY product_id";
            case "settings" -> "SELECT name,email,phone,address,city,postal_code,date_of_birth FROM customer WHERE customer_id=?";
            default -> throw new IllegalArgumentException("Unknown section.");
        };
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,customerId);try(ResultSet rs=ps.executeQuery()){
                var records=TransactionDAO.rows(rs);
                if("tickets".equals(section)) for(var ticket:records) ticket.put("messages",Jdbc.rows(c,
                    "SELECT sender_label,body,created_at FROM ticket_message WHERE ticket_id=? ORDER BY created_at,message_id",ticket.get("ticket_id")));
                return records;
            }
        }
    }
    public void blockCard(int customerId,int cardId) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE card c JOIN account a ON a.account_number=c.account_number SET c.status='BLOCKED' WHERE c.card_id=? AND a.customer_id=? AND c.status='ACTIVE'")) {
            ps.setInt(1,cardId);ps.setInt(2,customerId); if(ps.executeUpdate()!=1)throw new IllegalArgumentException("Active card not found. Refresh the page.");
        }
    }
    public void request(int customerId,String type,String description) throws SQLException {
        type=Input.choice(type,"ACCOUNT_OPENING","ACCOUNT_CLOSURE","PROFILE_UPDATE","CHEQUE_BOOK","ACCOUNT_STATEMENT");
        description=Input.text(description,500,"request description");
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO service_request(customer_id,request_type,description,status) VALUES(?,?,?,'PENDING')")) {
            ps.setInt(1,customerId);ps.setString(2,type);ps.setString(3,description);ps.executeUpdate();
        }
    }
    public void ticket(int customerId,String type,String subject,String description) throws SQLException {
        final String category=Input.choice(type,"INQUIRY","COMPLAINT","TECHNICAL","OTHER");
        final String title=Input.text(subject,150,"subject"),text=Input.text(description,5000,"description");
        Jdbc.transaction(c->{
            Jdbc.customer(c,customerId);
            long ticketId=Jdbc.insert(c,"INSERT INTO ticket(customer_id,ticket_type,subject,description,status) VALUES(?,?,?,?,'OPEN')",customerId,category,title,text);
            NotificationDAO.department(c,(int)ticketId,"CUSTOMER_SERVICE_OFFICER",null,null,"TICKET_CREATED","New Support Ticket","Support ticket #"+ticketId+" is awaiting review.");
            Jdbc.audit(c,null,"TICKET_CREATE","Customer "+customerId+"; ticket "+ticketId);
            return null;
        });
    }

    public void reply(int customerId,int ticketId,String body) throws SQLException {
        final String message=Input.text(body,5000,"reply");
        Jdbc.transaction(c->{
            Jdbc.customer(c,customerId);
            var ticket=Jdbc.one(c,"SELECT status FROM ticket WHERE ticket_id=? AND customer_id=? FOR UPDATE",ticketId,customerId);
            if("CLOSED".equals(ticket.get("status")))throw new IllegalArgumentException("This ticket is closed.");
            Jdbc.insert(c,"INSERT INTO ticket_message(ticket_id,sender_label,body) VALUES(?,'Customer',?)",ticketId,message);
            Jdbc.update(c,"UPDATE ticket SET date_updated=CURRENT_TIMESTAMP WHERE ticket_id=?",ticketId);
            NotificationDAO.customerReply(c,ticketId);
            Jdbc.audit(c,null,"TICKET_REPLY","Customer "+customerId+"; ticket "+ticketId);
            return null;
        });
    }

    public void profile(int customerId,String name,String phone,String address,String city,String postal) throws SQLException {
        name=Input.text(name,100,"name");phone=Input.text(phone,20,"phone");
        if(!phone.matches("[+0-9 ()-]{7,20}"))throw new IllegalArgumentException("Enter a valid phone number.");
        if(address==null || address.length()>255 || city==null || city.length()>100 || postal==null || postal.length()>20)throw new IllegalArgumentException("Profile field is too long.");
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE customer SET name=?,phone=?,address=?,city=?,postal_code=? WHERE customer_id=? AND status='ACTIVE'")) {
            ps.setString(1,name);ps.setString(2,phone);ps.setString(3,address.trim());ps.setString(4,city.trim());ps.setString(5,postal.trim());ps.setInt(6,customerId);
            if(ps.executeUpdate()!=1)throw new IllegalArgumentException("Active customer not found.");
        }
    }
    public void password(int customerId,String current,String next,String confirmation) throws SQLException {
        if(next==null || next.length()<8 || next.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72 || !next.equals(confirmation))throw new IllegalArgumentException("Passwords must match, contain at least 8 characters and use at most 72 UTF-8 bytes.");
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement ps=c.prepareStatement("SELECT password FROM customer WHERE customer_id=? AND status='ACTIVE' FOR UPDATE")) {
                    ps.setInt(1,customerId);try(ResultSet rs=ps.executeQuery()) {if(!rs.next() || !PasswordUtil.checkPassword(current,rs.getString(1)))throw new IllegalArgumentException("Current password is incorrect.");}
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE customer SET password=? WHERE customer_id=?")) {ps.setString(1,PasswordUtil.hashPassword(next));ps.setInt(2,customerId);if(ps.executeUpdate()!=1)throw new SQLException("Customer update failed");}
                c.commit();
            } catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
