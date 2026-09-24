package com.banking.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

final class FinancialLedger {
    static Map<String,Object> account(Connection c,int customer)throws SQLException {return Jdbc.one(c,"SELECT account_number,balance FROM account WHERE customer_id=? AND status='ACTIVE' ORDER BY account_number LIMIT 1 FOR UPDATE",customer);}
    static Map<String,Object> account(Connection c,int customer,long number)throws SQLException {return Jdbc.one(c,"SELECT account_number,balance FROM account WHERE account_number=? AND customer_id=? AND status='ACTIVE' FOR UPDATE",number,customer);}
    static void debit(Connection c,long account,BigDecimal amount)throws SQLException {if(amount.signum()<=0)throw new IllegalArgumentException("Invalid amount");Jdbc.exactlyOne(c,"UPDATE account SET balance=balance-? WHERE account_number=? AND status='ACTIVE' AND balance>=?",amount,account,amount);}
    static void credit(Connection c,long account,BigDecimal amount)throws SQLException {if(amount.signum()<=0)throw new IllegalArgumentException("Invalid amount");Jdbc.exactlyOne(c,"UPDATE account SET balance=balance+? WHERE account_number=? AND status='ACTIVE' AND balance<=9999999999999.99-?",amount,account,amount);}
    static void record(Connection c,long account,String type,String recipient,BigDecimal amount,String reference)throws SQLException {Jdbc.exactlyOne(c,"INSERT INTO payment(account_number,payment_type,recipient,amount,status,reference_number) VALUES(?,?,?,?,'COMPLETED',?)",account,type,recipient,amount,reference);NotificationDAO.payment(c,reference);}
    static long number(Map<String,Object> row){return ((Number)row.get("account_number")).longValue();}
}
