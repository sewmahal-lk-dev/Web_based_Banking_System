package com.banking.dao;

import com.banking.util.*;
import java.sql.*;

public class ServiceRequestDAO {
    public long create(int customer,String type,String description,String accountValue,String accountType,String email)throws SQLException {
        Input.choice(type,"ACCOUNT_OPENING","ACCOUNT_CLOSURE","PROFILE_UPDATE","CHEQUE_BOOK","ACCOUNT_STATEMENT");final String text=Input.text(description,500,"description");
        return Jdbc.transaction(c->{Jdbc.customer(c,customer);Long account=null;String selectedType=null,newEmail=null;
            if("ACCOUNT_OPENING".equals(type))selectedType=Input.choice(accountType,"SAVINGS","CURRENT");
            else if("PROFILE_UPDATE".equals(type))newEmail=AdminDAO.email(email);
            else {try{account=Long.parseLong(accountValue);}catch(RuntimeException e){throw new IllegalArgumentException("Select an account.");}FinancialLedger.account(c,customer,account);}
            long id=Jdbc.insert(c,"INSERT INTO service_request(customer_id,request_type,description,account_number,requested_account_type,requested_email,status) VALUES(?,?,?,?,?,?,'PENDING')",customer,type,text,account,selectedType,newEmail);
            NotificationDAO.submitted(c,NotificationDAO.Product.REQUEST,id);Jdbc.audit(c,null,"SERVICE_REQUEST","Customer "+customer+"; request "+id);return id;});
    }
    public void update(int customer,int id,String description,boolean cancel)throws SQLException {
        final String text=cancel?null:Input.text(description,500,"description");Jdbc.transaction(c->{Jdbc.customer(c,customer);
            if(cancel)Jdbc.exactlyOne(c,"UPDATE service_request SET status='CANCELLED' WHERE request_id=? AND customer_id=? AND status='PENDING'",id,customer);
            else Jdbc.exactlyOne(c,"UPDATE service_request SET description=? WHERE request_id=? AND customer_id=? AND status='PENDING'",text,id,customer);
            if(cancel)NotificationDAO.changed(c,NotificationDAO.Product.REQUEST,id);Jdbc.audit(c,null,cancel?"REQUEST_CANCEL":"REQUEST_UPDATE","Customer "+customer+"; request "+id);return null;});
    }
    public void ticketUpdate(int customer,int id,String subject,String description,boolean close)throws SQLException {
        final String title=close?null:Input.text(subject,150,"subject"),text=close?null:Input.text(description,5000,"description");
        Jdbc.transaction(c->{Jdbc.customer(c,customer);
            if(close)Jdbc.exactlyOne(c,"UPDATE ticket SET status='CLOSED' WHERE ticket_id=? AND customer_id=? AND status<>'CLOSED'",id,customer);
            else Jdbc.exactlyOne(c,"UPDATE ticket SET subject=?,description=? WHERE ticket_id=? AND customer_id=? AND status='OPEN' AND assigned_employee_id IS NULL",title,text,id,customer);
            Jdbc.audit(c,null,close?"TICKET_CLOSE":"TICKET_UPDATE","Customer "+customer+"; ticket "+id);return null;});
    }
    public void process(int employee,int id,String status,String reason)throws SQLException {
        Input.choice(status,"PROCESSING","REJECTED","COMPLETED");final String response=Input.text(reason,500,"response");
        Jdbc.transaction(c->{Jdbc.staff(c,employee,"CUSTOMER_SERVICE_OFFICER");var initial=Jdbc.one(c,"SELECT customer_id FROM service_request WHERE request_id=?",id);int customer=((Number)initial.get("customer_id")).intValue();Jdbc.customer(c,customer);
            var request=Jdbc.one(c,"SELECT request_type,account_number,requested_account_type,requested_email,status,response FROM service_request WHERE request_id=? AND status IN ('PENDING','PROCESSING','APPROVED') FOR UPDATE",id);
            String result=response;
            if("COMPLETED".equals(status)){
                switch((String)request.get("request_type")){
                    case "ACCOUNT_OPENING" -> {long number=AccountWorkflowDAO.open(c,customer,(String)request.get("requested_account_type"));Jdbc.exactlyOne(c,"UPDATE service_request SET account_number=? WHERE request_id=?",number,id);result="Account "+number+" opened. "+response;}
                    case "ACCOUNT_CLOSURE" -> {if(request.get("account_number")==null)throw new IllegalArgumentException("Request is missing its account. Ask the customer to resubmit.");AccountWorkflowDAO.close(c,customer,((Number)request.get("account_number")).longValue());}
                    case "PROFILE_UPDATE" -> {String email=AdminDAO.email((String)request.get("requested_email"));AdminDAO.uniqueEmail(c,email,customer,false);Jdbc.exactlyOne(c,"UPDATE customer SET email=? WHERE customer_id=?",email,customer);result="Email updated. "+response;}
                    case "ACCOUNT_STATEMENT" -> {FinancialLedger.account(c,customer,((Number)request.get("account_number")).longValue());result="Statement available to download. "+response;}
                    case "CHEQUE_BOOK" -> {var account=Jdbc.one(c,"SELECT account_type FROM account WHERE account_number=? AND customer_id=? AND status='ACTIVE'",request.get("account_number"),customer);if(!"CURRENT".equals(account.get("account_type")))throw new IllegalArgumentException("Cheque books require a current account.");result="Demo cheque-book order fulfilled. Collection details: "+response;}
                    default -> throw new IllegalArgumentException("Unsupported request.");
                }
            }
            Jdbc.exactlyOne(c,"UPDATE service_request SET status=?,response=?,rejection_reason=? WHERE request_id=?",status,result.substring(0,Math.min(500,result.length())),"REJECTED".equals(status)?response:null,id);if(!status.equals(request.get("status"))||!java.util.Objects.equals(result,request.get("response")))NotificationDAO.changed(c,NotificationDAO.Product.REQUEST,id);Jdbc.audit(c,employee,"REQUEST_"+status,"Request "+id+"; "+result);return null;});
    }
}
