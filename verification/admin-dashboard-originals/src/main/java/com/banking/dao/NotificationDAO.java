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
            var note=Jdbc.one(c,"SELECT related_ticket_id,notification_type FROM notification WHERE notification_id=? AND "+owner(customer)+"=? FOR UPDATE",id,user);
            String target=customer?"/customer/notifications":"/employee/notifications";
            Object ticketId=note.get("related_ticket_id");
            if(ticketId==null)target=bankingTarget(customer,(String)note.get("notification_type"),target);
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

    // Call only after a successful write, on the SAME transaction connection.
    // Notifications become visible only with the business commit; failures roll back both.
    enum Product {
        LOAN("loan", "loan_id", "Loan", "LOAN_OFFICER"),
        INVESTMENT("investment", "investment_id", "Investment", "INVESTMENT_OFFICER"),
        CARD("card", "card_id", "Card", "CARD_SERVICES_OFFICER"),
        REQUEST("service_request", "request_id", "Service request", "CUSTOMER_SERVICE_OFFICER");
        final String table,key,label,role;
        Product(String table,String key,String label,String role){this.table=table;this.key=key;this.label=label;this.role=role;}
    }
    private static String bankingTarget(boolean customer,String type,String fallback) {
        String section=null;
        if(type.startsWith("LOAN_"))section="loans";
        else if(type.startsWith("INVESTMENT_"))section="investments";
        else if(type.startsWith("CARD_"))section="cards";
        else if(type.startsWith("REQUEST_"))section="requests";
        else if(type.startsWith("PAYMENT_"))section="transactions";
        else if(type.startsWith("ACCOUNT_"))section="accounts";
        else if(type.startsWith("PROFILE_"))section="settings";
        else if(type.equals("EMPLOYEE_ACCESS"))return customer?fallback:"/employee/dashboard";
        return section==null?fallback:customer?"/customer/"+section:"/employee/dashboard";
    }
    private static void recipient(Connection c,int customer,String type,String title,String message)throws SQLException {
        Jdbc.exactlyOne(c,"INSERT INTO notification(customer_id,notification_type,title,message) VALUES(?,?,?,?)",customer,type,title,message);
    }
    private static void role(Connection c,String role,String type,String title,String message)throws SQLException {
        Jdbc.update(c,"INSERT INTO notification(employee_id,notification_type,title,message) SELECT employee_id,?,?,? FROM employee WHERE role=? AND status='ACTIVE'",type,title,message,role);
    }
    private static Map<String,Object> product(Connection c,Product product,long id)throws SQLException {
        // Table and column identifiers are enum constants, never request input.
        String sql=product==Product.CARD
            ?"SELECT a.customer_id,p.status,p.card_type FROM card p JOIN account a ON a.account_number=p.account_number WHERE p.card_id=?"
            :"SELECT customer_id,status FROM "+product.table+" WHERE "+product.key+"=?";
        return Jdbc.one(c,sql,id);
    }
    static void submitted(Connection c,Product product,long id)throws SQLException {
        var row=product(c,product,id);
        String name=(String)Jdbc.one(c,"SELECT name FROM customer WHERE customer_id=?",row.get("customer_id")).get("name");
        String label=product==Product.CARD?row.get("card_type").toString().toLowerCase(Locale.ROOT)+" card request":product.label.toLowerCase(Locale.ROOT)+(product==Product.LOAN?" application":product==Product.REQUEST?"":" request");
        role(c,product.role,product.name()+"_SUBMITTED","New "+label,"New "+label+" #"+id+" received from "+name+".");
    }
    static void changed(Connection c,Product product,long id)throws SQLException {changed(c,product,id,null);}
    static void changed(Connection c,Product product,long id,String action)throws SQLException {
        var row=product(c,product,id);String status=(String)row.get("status");
        String detail=switch(status){
            case "APPROVED"->"has been approved";
            case "REJECTED"->"has been rejected";
            case "ACTIVE"->product==Product.INVESTMENT?"has been approved and funded":product==Product.LOAN?"is now active; funds have been disbursed":"is now active";
            case "BLOCKED"->"has been blocked";
            case "CANCELLED"->"reject".equals(action)?"request has been rejected":"has been cancelled";
            case "CLOSED"->product==Product.INVESTMENT?"has been closed and its payout credited":product==Product.LOAN?"has been fully repaid and closed":"has been closed";
            case "PROCESSING"->"is being processed";
            case "COMPLETED"->"has been completed";
            default->"status is now "+status.toLowerCase(Locale.ROOT);
        };
        recipient(c,((Number)row.get("customer_id")).intValue(),product.name()+"_"+status,product.label+" update","Your "+product.label.toLowerCase(Locale.ROOT)+" #"+id+" "+detail+".");
    }
    static void accountChanged(Connection c,long number)throws SQLException {
        var row=Jdbc.one(c,"SELECT customer_id,status FROM account WHERE account_number=?",number);
        String status=(String)row.get("status"),masked="**** "+Long.toString(number).substring(Math.max(0,Long.toString(number).length()-4));
        recipient(c,((Number)row.get("customer_id")).intValue(),"ACCOUNT_"+status,"Account status updated","Your account "+masked+" is now "+status.toLowerCase(Locale.ROOT)+".");
        if("FROZEN".equals(status))role(c,"SYSTEM_ADMIN","ACCOUNT_FROZEN","Account frozen","Compliance has frozen account "+masked+" for customer #"+row.get("customer_id")+".");
    }
    static void profileChanged(Connection c,int customer)throws SQLException {
        String status=(String)Jdbc.one(c,"SELECT status FROM customer WHERE customer_id=?",customer).get("status");
        recipient(c,customer,"PROFILE_"+status,"Customer access updated","Your customer profile is now "+status.toLowerCase(Locale.ROOT)+".");
    }
    static void employeeAccess(Connection c,int employee)throws SQLException {
        var row=Jdbc.one(c,"SELECT name,role,status FROM employee WHERE employee_id=?",employee);
        role(c,"SYSTEM_ADMIN","EMPLOYEE_ACCESS","Employee access changed","Access for "+row.get("name")+" (#"+employee+") changed to "+row.get("role")+", "+row.get("status")+".");
    }
    static void payment(Connection c,String reference)throws SQLException {
        var row=Jdbc.one(c,"SELECT p.payment_id,p.payment_type,p.recipient,p.amount,p.status,a.customer_id FROM payment p JOIN account a ON a.account_number=p.account_number WHERE p.reference_number=? FOR UPDATE",reference);
        String status=(String)row.get("status");
        if(!List.of("COMPLETED","CANCELLED","FAILED").contains(status))return;
        String kind=row.get("payment_type").toString().toLowerCase(Locale.ROOT).replace('_',' ');
        String message="Your "+kind+" of LKR "+row.get("amount")+" "+("COMPLETED".equals(status)?"completed successfully":"was "+status.toLowerCase(Locale.ROOT))+". Payment #"+row.get("payment_id")+"; reference "+reference+".";
        paymentRecipient(c,((Number)row.get("customer_id")).intValue(),"PAYMENT_"+status,"Payment "+status.toLowerCase(Locale.ROOT),message);
        if("TRANSFER".equals(row.get("payment_type"))&&"COMPLETED".equals(status)) {
            // Transfers store the receiving account number, not a customer supplied recipient ID.
            var receiver=Jdbc.one(c,"SELECT customer_id FROM account WHERE account_number=?",row.get("recipient"));
            paymentRecipient(c,((Number)receiver.get("customer_id")).intValue(),"PAYMENT_RECEIVED","Transfer received","You received LKR "+row.get("amount")+". Payment #"+row.get("payment_id")+"; reference "+reference+".");
        }
    }
    private static void paymentRecipient(Connection c,int customer,String type,String title,String message)throws SQLException {
        // payment() holds the payment row lock. A repeated delivery for that recorded event
        // cannot create another notice, even after the original has been marked read.
        if(Jdbc.rows(c,"SELECT notification_id FROM notification WHERE customer_id=? AND notification_type=? AND message=? FOR UPDATE",customer,type,message).isEmpty())recipient(c,customer,type,title,message);
    }
    static void paymentId(Connection c,long id)throws SQLException {
        payment(c,(String)Jdbc.one(c,"SELECT reference_number FROM payment WHERE payment_id=?",id).get("reference_number"));
    }
}
