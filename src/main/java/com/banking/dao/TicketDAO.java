package com.banking.dao;

import com.banking.util.Input;
import java.sql.*;
import java.util.*;

/** Department queues and ticket changes. Identity and role are rechecked in MySQL. */
public class TicketDAO {
    public static final List<String> ROLES=List.of("CUSTOMER_SERVICE_OFFICER","LOAN_OFFICER","CARD_SERVICES_OFFICER","INVESTMENT_OFFICER","COMPLIANCE_RISK_OFFICER");
    public static String department(String role) {
        if(role==null)return "Awaiting assignment";
        return switch(role) {
            case "CUSTOMER_SERVICE_OFFICER" -> "Customer Service";
            case "LOAN_OFFICER" -> "Loan Department";
            case "CARD_SERVICES_OFFICER" -> "Card Services";
            case "INVESTMENT_OFFICER" -> "Investment Department";
            case "COMPLIANCE_RISK_OFFICER" -> "Compliance & Risk";
            default -> "Awaiting assignment";
        };
    }
    private static String staff(Connection c,int employee)throws SQLException {
        String role=(String)Jdbc.one(c,"SELECT role FROM employee WHERE employee_id=? AND status='ACTIVE' FOR UPDATE",employee).get("role");
        if(!ROLES.contains(role))throw new SecurityException("You cannot manage support tickets.");
        return role;
    }
    public List<Map<String,Object>> list(int employee)throws SQLException {
        return Jdbc.transaction(c->{
            String role=staff(c,employee);
            String sql="SELECT t.*,u.name AS customer_name,u.email AS customer_email,e.name AS assigned_officer FROM ticket t JOIN customer u ON u.customer_id=t.customer_id LEFT JOIN employee e ON e.employee_id=t.assigned_employee_id";
            var tickets="CUSTOMER_SERVICE_OFFICER".equals(role)
                ?Jdbc.rows(c,sql+" ORDER BY t.ticket_id DESC LIMIT 200")
                :Jdbc.rows(c,sql+" WHERE t.assigned_role=? ORDER BY t.ticket_id DESC LIMIT 200",role);
            for(var ticket:tickets)ticket.put("messages",Jdbc.rows(c,"SELECT sender_label,body,created_at FROM ticket_message WHERE ticket_id=? ORDER BY created_at,message_id",ticket.get("ticket_id")));
            return tickets;
        });
    }
    public void assign(int employee,int ticketId,String destination)throws SQLException {
        final String target=Input.choice(destination,ROLES.toArray(String[]::new));
        Jdbc.transaction(c->{
            if(!"CUSTOMER_SERVICE_OFFICER".equals(staff(c,employee)))throw new SecurityException("Only Customer Service may assign tickets.");
            var ticket=Jdbc.one(c,"SELECT status,assigned_role FROM ticket WHERE ticket_id=? FOR UPDATE",ticketId);
            if(!List.of("OPEN","ASSIGNED","IN_PROGRESS","ESCALATED").contains(ticket.get("status")))throw new IllegalArgumentException("Resolved or closed tickets cannot be reassigned.");
            if(target.equals(ticket.get("assigned_role")))throw new IllegalArgumentException("This ticket is already assigned to that department.");
            Jdbc.exactlyOne(c,"UPDATE ticket SET assigned_role=?,assigned_employee_id=NULL,status='ASSIGNED' WHERE ticket_id=?",target,ticketId);
            Jdbc.insert(c,"INSERT INTO ticket_message(ticket_id,sender_label,body) VALUES(?,'Support update',?)",ticketId,"Assigned to "+department(target)+". Status: ASSIGNED.");
            NotificationDAO.customer(c,ticketId,"TICKET_ASSIGNED","Ticket Assigned","Your support ticket #"+ticketId+" has been assigned to "+department(target)+".");
            NotificationDAO.department(c,ticketId,target,null,employee,"TICKET_ASSIGNED","New Assigned Support Ticket","Support ticket #"+ticketId+" has been assigned to "+department(target)+".");
            Jdbc.audit(c,employee,"TICKET_ASSIGN","Ticket "+ticketId+"; destination "+target);
            return null;
        });
    }
    public void update(int employee,int ticketId,String action,String next,String body)throws SQLException {
        final String operation=Input.choice(action,"ticket-response","ticket-status","ticket-reply");
        final String message="ticket-status".equals(operation)?null:Input.text(body,5000,"reply");
        final String requested="ticket-reply".equals(operation)?null:Input.choice(next,"IN_PROGRESS","ESCALATED","RESOLVED","CLOSED");
        Jdbc.transaction(c->{
            String role=staff(c,employee);
            var ticket=Jdbc.one(c,"SELECT status,assigned_role FROM ticket WHERE ticket_id=? FOR UPDATE",ticketId);
            String destination=(String)ticket.get("assigned_role"),state=(String)ticket.get("status");
            if(!(role.equals(destination)||(destination==null&&"CUSTOMER_SERVICE_OFFICER".equals(role))))throw new SecurityException("This ticket belongs to another department.");
            boolean closing="ticket-status".equals(operation)&&"CLOSED".equals(requested)&&"RESOLVED".equals(state);
            if("CLOSED".equals(requested)&&!closing)throw new IllegalArgumentException("Resolve this ticket before closing it.");
            if(!closing&&!List.of("OPEN","ASSIGNED","IN_PROGRESS","ESCALATED").contains(state))throw new IllegalArgumentException("Resolved or closed tickets cannot be changed.");
            if("RESOLVED".equals(requested)&&!List.of("IN_PROGRESS","ESCALATED").contains(state))throw new IllegalArgumentException("Start work on this ticket before resolving it.");
            String newState=requested==null?state:requested;
            Jdbc.exactlyOne(c,"UPDATE ticket SET status=?,assigned_role=?,assigned_employee_id=?,response=COALESCE(?,response),date_updated=CURRENT_TIMESTAMP WHERE ticket_id=?",newState,role,employee,message,ticketId);
            if(message!=null)Jdbc.insert(c,"INSERT INTO ticket_message(ticket_id,sender_label,body) VALUES(?,?,?)",ticketId,"CUSTOMER_SERVICE_OFFICER".equals(role)?"Support Officer":department(role)+" Officer",message);
            if(message==null||!Objects.equals(state,newState))Jdbc.insert(c,"INSERT INTO ticket_message(ticket_id,sender_label,body) VALUES(?,'Support update',?)",ticketId,"Status: "+newState.replace('_',' ')+".");
            if(message!=null)NotificationDAO.customer(c,ticketId,"SUPPORT_REPLY","New Support Reply",department(role)+" replied to your support ticket #"+ticketId+".");
            if(!Objects.equals(state,newState)) {
                NotificationDAO.customer(c,ticketId,"RESOLVED".equals(newState)?"TICKET_RESOLVED":"TICKET_STATUS","RESOLVED".equals(newState)?"Ticket Resolved":"Ticket Status Updated","RESOLVED".equals(newState)?"Your support ticket #"+ticketId+" has been resolved. Please review the response.":"Your support ticket #"+ticketId+" is now "+newState.replace('_',' ')+".");
                if("ESCALATED".equals(newState))NotificationDAO.department(c,ticketId,role,null,employee,"TICKET_ESCALATED","Support Ticket Escalated","Support ticket #"+ticketId+" needs further review in "+department(role)+".");
            }
            Jdbc.audit(c,employee,message==null?"TICKET_STATUS":"TICKET_REPLY","Ticket "+ticketId+"; department "+role+"; status "+newState);
            return null;
        });
    }
}
