package com.banking.dao;

import com.banking.util.DBConnection;
import java.sql.*;
import java.util.*;

/** Recipient identity comes from the authenticated session, never form parameters. */
public class NotificationDAO {
    private static String owner(boolean customer){return customer?"customer_id":"employee_id";}
    public List<Map<String,Object>> list(int user,boolean customer,int offset,int limit)throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            return Jdbc.rows(c,"SELECT * FROM notification WHERE "+owner(customer)+"=? ORDER BY notification_id DESC LIMIT ? OFFSET ?",user,limit,offset);
        }
    }
    public long unread(int user,boolean customer)throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            return ((Number)Jdbc.one(c,"SELECT COUNT(*) AS total FROM notification WHERE "+owner(customer)+"=? AND is_read=FALSE",user).get("total")).longValue();
        }
    }
    public void markAll(int user,boolean customer)throws SQLException {
        Jdbc.transaction(c->{Jdbc.update(c,"UPDATE notification SET is_read=TRUE,read_at=CURRENT_TIMESTAMP WHERE "+owner(customer)+"=? AND is_read=FALSE",user);return null;});
    }
    public String open(int user,boolean customer,long id)throws SQLException {
        if(id<=0)throw new IllegalArgumentException("Invalid notification ID.");
        return Jdbc.transaction(c->{
            var note=Jdbc.one(c,"SELECT related_ticket_id FROM notification WHERE notification_id=? AND "+owner(customer)+"=? FOR UPDATE",id,user);
            String target=customer?"/customer/notifications":"/employee/notifications";
            Object ticketId=note.get("related_ticket_id");
            if(ticketId!=null) {
                var tickets=Jdbc.rows(c,"SELECT customer_id,assigned_role FROM ticket WHERE ticket_id=?",ticketId);
                if(!tickets.isEmpty()) {
                    var ticket=tickets.get(0);
                    if(customer&&((Number)ticket.get("customer_id")).intValue()==user)target="/customer/tickets?ticket="+ticketId+"#ticket-"+ticketId;
                    if(!customer) {
                        String role=(String)Jdbc.one(c,"SELECT role FROM employee WHERE employee_id=? AND status='ACTIVE'",user).get("role");
                        if(TicketDAO.ROLES.contains(role)&&("CUSTOMER_SERVICE_OFFICER".equals(role)||role.equals(ticket.get("assigned_role"))))target="/employee/dashboard?ticket="+ticketId+"#support-ticket-"+ticketId;
                    }
                }
            }
            Jdbc.update(c,"UPDATE notification SET is_read=TRUE,read_at=COALESCE(read_at,CURRENT_TIMESTAMP) WHERE notification_id=? AND "+owner(customer)+"=?",id,user);
            return target;
        });
    }
    static void customer(Connection c,int ticket,String type,String title,String message)throws SQLException {
        Jdbc.update(c,"INSERT INTO notification(customer_id,notification_type,title,message,related_ticket_id) SELECT customer_id,?,?,?,ticket_id FROM ticket WHERE ticket_id=?",type,title,message,ticket);
    }
    static void department(Connection c,int ticket,String role,Integer specific,Integer actor,String type,String title,String message)throws SQLException {
        Jdbc.update(c,"INSERT INTO notification(employee_id,notification_type,title,message,related_ticket_id) SELECT employee_id,?,?,?,? FROM employee WHERE status='ACTIVE' AND role=? AND (? IS NULL OR employee_id=?) AND (? IS NULL OR employee_id<>?)",type,title,message,ticket,role,specific,specific,actor,actor);
    }
    static void customerReply(Connection c,int ticket)throws SQLException {
        var item=Jdbc.one(c,"SELECT assigned_role,assigned_employee_id FROM ticket WHERE ticket_id=?",ticket);
        String role=(String)item.get("assigned_role");
        // assigned_employee_id is the last handler in V6; ownership remains department-wide.
        department(c,ticket,role==null?"CUSTOMER_SERVICE_OFFICER":role,null,null,"CUSTOMER_REPLY","New Customer Reply","A customer replied to support ticket #"+ticket+".");
    }
}
