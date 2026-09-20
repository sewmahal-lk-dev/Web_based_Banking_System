package com.banking.dao;

import com.banking.util.DBConnection;
import com.banking.util.Input;
import java.sql.*;
import java.math.BigDecimal;
import java.util.UUID;

/** Pays an existing installment exactly as recorded by the bank. Does not calculate interest. */
public class LoanRepaymentDAO {
    public void pay(int customerId,int repaymentId) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                long account;BigDecimal balance,amount;
                try(PreparedStatement ps=c.prepareStatement("SELECT account_number,balance FROM account WHERE customer_id=? AND status='ACTIVE' ORDER BY account_number LIMIT 1 FOR UPDATE")) {
                    ps.setInt(1,customerId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("No active account found.");account=rs.getLong(1);balance=rs.getBigDecimal(2);}
                }
                try(PreparedStatement ps=c.prepareStatement("SELECT r.amount FROM loan_repayment r JOIN loan l ON l.loan_id=r.loan_id WHERE r.repayment_id=? AND l.customer_id=? AND l.status='ACTIVE' AND r.status IN ('PENDING','OVERDUE') FOR UPDATE")) {
                    ps.setInt(1,repaymentId);ps.setInt(2,customerId);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Unpaid installment on an active loan not found.");amount=Input.money(rs.getBigDecimal(1));}
                }
                if(balance==null || balance.compareTo(amount)<0)throw new IllegalArgumentException("Insufficient account balance.");
                try(PreparedStatement ps=c.prepareStatement("UPDATE account SET balance=balance-? WHERE account_number=?")){ps.setBigDecimal(1,amount);ps.setLong(2,account);if(ps.executeUpdate()!=1)throw new SQLException("Debit failed");}
                try(PreparedStatement ps=c.prepareStatement("UPDATE loan_repayment SET status='PAID',paid_date=CURDATE() WHERE repayment_id=? AND status IN ('PENDING','OVERDUE')")){ps.setInt(1,repaymentId);if(ps.executeUpdate()!=1)throw new SQLException("Installment update failed");}
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO payment(account_number,payment_type,recipient,amount,status,reference_number) VALUES(?,'LOAN_PAYMENT',?,?,'COMPLETED',?)")) {
                    ps.setLong(1,account);ps.setString(2,"Loan installment "+repaymentId);ps.setBigDecimal(3,amount);ps.setString(4,"LNP-"+repaymentId);if(ps.executeUpdate()!=1)throw new SQLException("Payment recording failed");
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE loan SET status='CLOSED' WHERE loan_id=(SELECT loan_id FROM loan_repayment WHERE repayment_id=?) AND status='ACTIVE' AND NOT EXISTS (SELECT 1 FROM loan_repayment r WHERE r.loan_id=loan.loan_id AND r.status<>'PAID')")){ps.setInt(1,repaymentId);ps.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
