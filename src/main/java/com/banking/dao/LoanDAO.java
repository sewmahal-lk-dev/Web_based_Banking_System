package com.banking.dao;

import com.banking.util.*;
import java.math.*;
import java.sql.*;
import java.time.LocalDate;

public class LoanDAO {
    public long apply(int customer,String type,String amount,String months)throws SQLException {
        Input.choice(type,"STUDENT","PERSONAL","VEHICLE","HOME","BUSINESS");BigDecimal principal=DemoRules.amount("loan",amount);int term=DemoRules.months("loan",months);
        return Jdbc.transaction(c->{Jdbc.customer(c,customer);long account=FinancialLedger.number(FinancialLedger.account(c,customer));
            long id=Jdbc.insert(c,"INSERT INTO loan(customer_id,account_number,loan_type,amount,interest_rate,term_months,application_date,status) VALUES(?,?,?,?,?,?,CURDATE(),'PENDING')",customer,account,type,principal,DemoRules.number("loan.annualRate"),term);
            NotificationDAO.submitted(c,NotificationDAO.Product.LOAN,id);Jdbc.audit(c,null,"LOAN_APPLICATION","Customer "+customer+"; loan "+id);return id;});
    }
    public void update(int customer,int loan,String amount,String months)throws SQLException {
        BigDecimal principal=DemoRules.amount("loan",amount);int term=DemoRules.months("loan",months);
        Jdbc.transaction(c->{Jdbc.customer(c,customer);Jdbc.exactlyOne(c,"UPDATE loan SET amount=?,term_months=? WHERE loan_id=? AND customer_id=? AND status='PENDING'",principal,term,loan,customer);Jdbc.audit(c,null,"LOAN_UPDATE","Customer "+customer+"; loan "+loan);return null;});
    }
    public void cancel(int customer,int loan)throws SQLException {
        Jdbc.transaction(c->{Jdbc.customer(c,customer);Jdbc.exactlyOne(c,"UPDATE loan SET status='CANCELLED' WHERE loan_id=? AND customer_id=? AND status IN ('PENDING','APPROVED')",loan,customer);NotificationDAO.changed(c,NotificationDAO.Product.LOAN,loan);Jdbc.audit(c,null,"LOAN_CANCEL","Customer "+customer+"; loan "+loan);return null;});
    }
    public void decision(int employee,int loan,boolean approve,String reason)throws SQLException {
        reason=Input.text(reason,500,"decision reason");final String note=reason;
        Jdbc.transaction(c->{Jdbc.staff(c,employee,"LOAN_OFFICER");Jdbc.exactlyOne(c,"UPDATE loan SET status=?,employee_id=?,rejection_reason=? WHERE loan_id=? AND status='PENDING'",approve?"APPROVED":"REJECTED",employee,note,loan);NotificationDAO.changed(c,NotificationDAO.Product.LOAN,loan);Jdbc.audit(c,employee,approve?"LOAN_APPROVE":"LOAN_REJECT","Loan "+loan+"; "+note);return null;});
    }
    public void accept(int customer,int loan)throws SQLException {
        Jdbc.transaction(c->{Jdbc.customer(c,customer);
            var details=Jdbc.one(c,"SELECT account_number FROM loan WHERE loan_id=? AND customer_id=? AND status='APPROVED'",loan,customer);
            if(details.get("account_number")==null)throw new IllegalArgumentException("This legacy loan has no recorded disbursement account.");
            long account=((Number)details.get("account_number")).longValue();FinancialLedger.account(c,customer,account);
            var row=Jdbc.one(c,"SELECT amount,interest_rate,term_months FROM loan WHERE loan_id=? AND customer_id=? AND status='APPROVED' FOR UPDATE",loan,customer);
            BigDecimal principal=(BigDecimal)row.get("amount"),rate=(BigDecimal)row.get("interest_rate");int months=((Number)row.get("term_months")).intValue();
            FinancialLedger.credit(c,account,principal);FinancialLedger.record(c,account,"LOAN_DISBURSEMENT","Loan "+loan,principal,"LOAN-DISBURSE-"+loan);
            LocalDate start=LocalDate.now();int installment=0;
            for(BigDecimal amount:DemoRules.installments(principal,rate,months)){installment++;Jdbc.exactlyOne(c,"INSERT INTO loan_repayment(loan_id,amount,due_date,status) VALUES(?,?,?,'PENDING')",loan,amount,Date.valueOf(start.plusMonths(installment)));}
            Jdbc.exactlyOne(c,"UPDATE loan SET status='ACTIVE' WHERE loan_id=? AND status='APPROVED'",loan);NotificationDAO.changed(c,NotificationDAO.Product.LOAN,loan);Jdbc.audit(c,null,"LOAN_ACCEPT_DISBURSE","Customer "+customer+"; loan "+loan);return null;});
    }
}
