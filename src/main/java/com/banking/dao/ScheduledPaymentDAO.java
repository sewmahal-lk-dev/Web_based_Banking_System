package com.banking.dao;

import com.banking.util.*;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;

public class ScheduledPaymentDAO {
    public List<Map<String,Object>> list(int customer)throws SQLException {try(Connection c=DBConnection.getConnection()){return Jdbc.rows(c,"SELECT p.payment_id,p.recipient,p.bill_reference,p.amount,p.scheduled_date,p.status,p.reference_number FROM payment p JOIN account a ON a.account_number=p.account_number WHERE a.customer_id=? AND p.scheduled_date IS NOT NULL ORDER BY p.payment_id DESC LIMIT 100",customer);}}
    public long save(int customer,Long id,String recipient,String billReference,String amount,String date)throws SQLException {
        final String provider=Input.text(recipient,150,"provider"),bill=PaymentDAO.validateBillReference(billReference);BigDecimal value;LocalDateTime when;
        try{value=Input.money(new BigDecimal(amount));when=LocalDateTime.parse(date);}catch(RuntimeException e){throw new IllegalArgumentException("Enter a valid amount and scheduled date/time.");}
        if(!when.isAfter(LocalDateTime.now()) || when.isAfter(LocalDateTime.now().plusYears(1)))throw new IllegalArgumentException("Choose a future date within one year.");
        final BigDecimal money=value;final Timestamp scheduled=Timestamp.valueOf(when);
        return Jdbc.transaction(c->{Jdbc.customer(c,customer);long account=FinancialLedger.number(FinancialLedger.account(c,customer));long saved;
            if(id==null)saved=Jdbc.insert(c,"INSERT INTO payment(account_number,payment_type,recipient,amount,scheduled_date,status,reference_number,bill_reference) VALUES(?,'BILL_PAYMENT',?,?,?,'PENDING',?,?)",account,provider,money,scheduled,"SCH-"+UUID.randomUUID(),bill);
            else {Jdbc.exactlyOne(c,"UPDATE payment p JOIN account a ON a.account_number=p.account_number SET p.recipient=?,p.amount=?,p.scheduled_date=?,p.bill_reference=? WHERE p.payment_id=? AND a.customer_id=? AND p.status='PENDING' AND p.scheduled_date IS NOT NULL AND p.payment_type='BILL_PAYMENT'",provider,money,scheduled,bill,id,customer);saved=id;}
            Jdbc.audit(c,null,"PAYMENT_SCHEDULE","Customer "+customer+"; payment "+saved);return saved;});
    }
    public void cancel(int customer,long id)throws SQLException {Jdbc.transaction(c->{Jdbc.customer(c,customer);Jdbc.exactlyOne(c,"UPDATE payment p JOIN account a ON a.account_number=p.account_number SET p.status='CANCELLED' WHERE p.payment_id=? AND a.customer_id=? AND p.status='PENDING' AND p.scheduled_date IS NOT NULL AND p.payment_type='BILL_PAYMENT'",id,customer);NotificationDAO.paymentId(c,id);Jdbc.audit(c,null,"PAYMENT_CANCEL","Customer "+customer+"; payment "+id);return null;});}
    public void execute(int customer,long id)throws SQLException {
        Jdbc.transaction(c->{Jdbc.customer(c,customer);var initial=Jdbc.one(c,"SELECT p.account_number FROM payment p JOIN account a ON a.account_number=p.account_number WHERE p.payment_id=? AND a.customer_id=?",id,customer);long account=FinancialLedger.number(initial);FinancialLedger.account(c,customer,account);
            var row=Jdbc.one(c,"SELECT amount FROM payment WHERE payment_id=? AND status='PENDING' AND payment_type='BILL_PAYMENT' AND scheduled_date<=NOW() FOR UPDATE",id);
            FinancialLedger.debit(c,account,(BigDecimal)row.get("amount"));Jdbc.exactlyOne(c,"UPDATE payment SET status='COMPLETED',payment_date=NOW() WHERE payment_id=? AND status='PENDING'",id);NotificationDAO.paymentId(c,id);Jdbc.audit(c,null,"SCHEDULED_PAYMENT_EXECUTE","Customer "+customer+"; payment "+id);return null;});
    }
}
