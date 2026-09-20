package com.banking.dao;

import com.banking.util.*;
import java.math.*;
import java.sql.*;
import java.time.*;

public class InvestmentDAO {
    public long apply(int customer,String type,String amount,String months)throws SQLException {
        Input.choice(type,"FIXED_DEPOSIT","SAVINGS_PLAN");BigDecimal principal=DemoRules.amount("investment",amount);int term=DemoRules.months("investment",months);
        return Jdbc.transaction(c->{Jdbc.customer(c,customer);long account=FinancialLedger.number(FinancialLedger.account(c,customer));
            long id=Jdbc.insert(c,"INSERT INTO investment(customer_id,account_number,investment_type,amount,interest_rate,term_months,start_date,maturity_date,status) VALUES(?,?,?,?,?,?,CURDATE(),?,'PENDING')",customer,account,type,principal,DemoRules.number("investment.annualRate"),term,Date.valueOf(LocalDate.now().plusMonths(term)));
            Jdbc.audit(c,null,"INVESTMENT_REQUEST","Customer "+customer+"; investment "+id);return id;});
    }
    public void update(int customer,int id,String amount,String months)throws SQLException {
        BigDecimal principal=DemoRules.amount("investment",amount);int term=DemoRules.months("investment",months);
        Jdbc.transaction(c->{Jdbc.customer(c,customer);Jdbc.exactlyOne(c,"UPDATE investment SET amount=?,term_months=?,maturity_date=? WHERE investment_id=? AND customer_id=? AND status='PENDING'",principal,term,Date.valueOf(LocalDate.now().plusMonths(term)),id,customer);return null;});
    }
    public void cancel(int customer,int id)throws SQLException {Jdbc.transaction(c->{Jdbc.customer(c,customer);Jdbc.exactlyOne(c,"UPDATE investment SET status='CANCELLED' WHERE investment_id=? AND customer_id=? AND status='PENDING'",id,customer);Jdbc.audit(c,null,"INVESTMENT_CANCEL","Customer "+customer+"; investment "+id);return null;});}
    public void process(int employee,int id,boolean approve,String reason)throws SQLException {
        final String note=Input.text(reason,500,"reason");
        Jdbc.transaction(c->{Jdbc.staff(c,employee,"INVESTMENT_OFFICER");
            var initial=Jdbc.one(c,"SELECT customer_id,account_number FROM investment WHERE investment_id=? AND status='PENDING'",id);int customer=((Number)initial.get("customer_id")).intValue();Jdbc.customer(c,customer);
            if(!approve){Jdbc.exactlyOne(c,"UPDATE investment SET status='REJECTED',employee_id=? WHERE investment_id=? AND status='PENDING'",employee,id);Jdbc.audit(c,employee,"INVESTMENT_REJECT","Investment "+id+"; "+note);return null;}
            if(initial.get("account_number")==null)throw new IllegalArgumentException("This legacy investment has no recorded funding account.");
            long account=((Number)initial.get("account_number")).longValue();var fundingAccount=FinancialLedger.account(c,customer,account);
            var row=Jdbc.one(c,"SELECT amount,term_months FROM investment WHERE investment_id=? AND status='PENDING' FOR UPDATE",id);BigDecimal principal=(BigDecimal)row.get("amount");int months=((Number)row.get("term_months")).intValue();
            if(((BigDecimal)fundingAccount.get("balance")).compareTo(principal)<0)throw new IllegalArgumentException("Insufficient account balance to fund this investment.");
            FinancialLedger.debit(c,account,principal);FinancialLedger.record(c,account,"INVESTMENT_FUNDING","Investment "+id,principal,"INV-FUND-"+id);
            Jdbc.exactlyOne(c,"UPDATE investment SET status='ACTIVE',employee_id=?,start_date=CURDATE(),maturity_date=? WHERE investment_id=? AND status='PENDING'",employee,Date.valueOf(LocalDate.now().plusMonths(months)),id);
            Jdbc.audit(c,employee,"INVESTMENT_FUND","Investment "+id+"; "+note);return null;});
    }
    public void withdraw(int customer,int id)throws SQLException {Jdbc.transaction(c->{Jdbc.customer(c,customer);payout(c,customer,id,null,false);return null;});}
    public void mature(int employee,int id)throws SQLException {Jdbc.transaction(c->{Jdbc.staff(c,employee,"INVESTMENT_OFFICER");var row=Jdbc.one(c,"SELECT customer_id FROM investment WHERE investment_id=?",id);int customer=((Number)row.get("customer_id")).intValue();Jdbc.customer(c,customer);payout(c,customer,id,employee,true);return null;});}
    private void payout(Connection c,int customer,int id,Integer employee,boolean maturityOnly)throws SQLException {
        var initial=Jdbc.one(c,"SELECT account_number FROM investment WHERE investment_id=? AND customer_id=? AND status IN ('ACTIVE','MATURED')",id,customer);
        if(initial.get("account_number")==null)throw new IllegalArgumentException("This legacy investment has no recorded payout account.");
        long account=((Number)initial.get("account_number")).longValue();FinancialLedger.account(c,customer,account);
        var row=Jdbc.one(c,"SELECT amount,interest_rate,start_date,maturity_date FROM investment WHERE investment_id=? AND customer_id=? AND status IN ('ACTIVE','MATURED') FOR UPDATE",id,customer);
        if(row.get("interest_rate")==null || row.get("maturity_date")==null)throw new IllegalArgumentException("Recorded investment terms are incomplete.");
        LocalDate start=((Date)row.get("start_date")).toLocalDate(),maturity=((Date)row.get("maturity_date")).toLocalDate();
        if(maturityOnly && LocalDate.now().isBefore(maturity))throw new IllegalArgumentException("Investment has not matured.");
        BigDecimal value=DemoRules.payout((BigDecimal)row.get("amount"),(BigDecimal)row.get("interest_rate"),start,maturity,LocalDate.now());
        FinancialLedger.credit(c,account,value);FinancialLedger.record(c,account,"INVESTMENT_PAYOUT","Investment "+id,value,"INV-PAYOUT-"+id);
        Jdbc.exactlyOne(c,"UPDATE investment SET status='CLOSED',payout_amount=? WHERE investment_id=? AND status IN ('ACTIVE','MATURED')",value,id);Jdbc.audit(c,employee,"INVESTMENT_PAYOUT","Customer "+customer+"; investment "+id+"; amount "+value);
    }
}
