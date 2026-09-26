package com.banking.dao;

import com.banking.util.*;
import java.sql.*;
import java.security.SecureRandom;

public class AccountWorkflowDAO {
    private static final SecureRandom RANDOM=new SecureRandom();
    static long open(Connection c,int customer,String type)throws SQLException {
        Input.choice(type,"SAVINGS","CURRENT");Jdbc.customer(c,customer);
        long number=2_000_000_000_000L+RANDOM.nextLong(7_000_000_000_000L);
        Jdbc.exactlyOne(c,"INSERT INTO account(account_number,customer_id,account_type,balance,status,open_date) VALUES(?,?,?,0,'ACTIVE',CURDATE())",number,customer,type);return number;
    }
    static void close(Connection c,int customer,long number)throws SQLException {
        Jdbc.customer(c,customer);var account=Jdbc.one(c,"SELECT balance,status FROM account WHERE account_number=? AND customer_id=? FOR UPDATE",number,customer);
        if(!java.util.List.of("ACTIVE","INACTIVE").contains(account.get("status")) || ((java.math.BigDecimal)account.get("balance")).signum()!=0)throw new IllegalArgumentException("Only an unfrozen account with zero balance can be closed.");
        if(!Jdbc.rows(c,"SELECT card_id FROM card WHERE account_number=? AND status IN ('PENDING','ACTIVE','BLOCKED')",number).isEmpty())throw new IllegalArgumentException("Cancel or close the account's cards first.");
        if(!Jdbc.rows(c,"SELECT payment_id FROM payment WHERE account_number=? AND status='PENDING'",number).isEmpty())throw new IllegalArgumentException("Cancel pending payments first.");
        if(!Jdbc.rows(c,"SELECT loan_id FROM loan WHERE customer_id=? AND status IN ('PENDING','APPROVED','ACTIVE')",customer).isEmpty() || !Jdbc.rows(c,"SELECT investment_id FROM investment WHERE customer_id=? AND status IN ('PENDING','ACTIVE','MATURED')",customer).isEmpty())throw new IllegalArgumentException("Settle or cancel outstanding loans and investments before closing accounts.");
        Jdbc.exactlyOne(c,"UPDATE account SET status='CLOSED' WHERE account_number=?",number);
    }
    public void deactivate(int customer,String password)throws SQLException {
        Jdbc.transaction(c->{var row=Jdbc.one(c,"SELECT password FROM customer WHERE customer_id=? AND status='ACTIVE' FOR UPDATE",customer);if(!PasswordUtil.checkPassword(password,(String)row.get("password")))throw new IllegalArgumentException("Current password is incorrect.");deactivate(c,customer);Jdbc.audit(c,null,"CUSTOMER_DEACTIVATE","Customer "+customer);return null;});
    }
    static void deactivate(Connection c,int customer)throws SQLException {
        if(!Jdbc.rows(c,"SELECT account_number FROM account WHERE customer_id=? AND status<>'CLOSED'",customer).isEmpty())throw new IllegalArgumentException("Close all banking accounts before deactivating the profile.");
        if(!Jdbc.rows(c,"SELECT loan_id FROM loan WHERE customer_id=? AND status IN ('PENDING','APPROVED','ACTIVE')",customer).isEmpty() || !Jdbc.rows(c,"SELECT investment_id FROM investment WHERE customer_id=? AND status IN ('PENDING','ACTIVE','MATURED')",customer).isEmpty())throw new IllegalArgumentException("Outstanding products prevent profile deactivation.");
        Jdbc.exactlyOne(c,"UPDATE customer SET status='INACTIVE' WHERE customer_id=? AND status='ACTIVE'",customer);
    }
}
