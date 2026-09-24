package com.banking.dao;

import com.banking.util.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.security.SecureRandom;

public class CardDAO {
    public java.util.List<java.util.Map<String,Object>> findForClosure(String id, String number) throws SQLException {
        id = id == null ? "" : id.trim();
        number = number == null ? "" : number.trim().replace(" ", "");
        if (id.isEmpty() == number.isEmpty()) {
            throw new IllegalArgumentException("Enter either a card ID or a card number.");
        }
        String condition;
        Object value;
        if (!id.isEmpty()) {
            condition = "c.card_id=?";
            value = Input.id(id);
        } else {
            if (number.matches("\\*+[0-9]{4}")) number = number.replace("*", "");
            if (!number.matches("[0-9]{4}|[0-9]{12,19}|DEMO[0-9]{15}")) {
                throw new IllegalArgumentException("Enter a full card number or its last four digits.");
            }
            condition = number.length() == 4 ? "RIGHT(c.card_number,4)=?" : "c.card_number=?";
            value = number;
        }
        try (Connection c = DBConnection.getConnection()) {
            return Jdbc.rows(c, "SELECT c.card_id, CONCAT('**** ',RIGHT(c.card_number,4)) AS masked_number, " +
                    "c.status,c.account_number,u.name AS customer_name FROM card c " +
                    "JOIN account a ON a.account_number=c.account_number " +
                    "JOIN customer u ON u.customer_id=a.customer_id WHERE " + condition + " ORDER BY c.card_id LIMIT 51", value);
        }
    }
    private static final SecureRandom RANDOM=new SecureRandom();
    public long apply(int customer,String type)throws SQLException {
        Input.choice(type,"DEBIT","CREDIT");
        return Jdbc.transaction(c->{Jdbc.customer(c,customer);long account=FinancialLedger.number(FinancialLedger.account(c,customer));
            if(!Jdbc.rows(c,"SELECT card_id FROM card WHERE account_number=? AND card_type=? AND status IN ('PENDING','ACTIVE','BLOCKED')",account,type).isEmpty())throw new IllegalArgumentException("You already have an open card or request of this type on this account.");
            String number="DEMO"+String.format("%015d",RANDOM.nextLong(1_000_000_000_000_000L));
            long id=Jdbc.insert(c,"INSERT INTO card(account_number,card_number,card_type,issue_date,expiry_date,status,daily_limit) VALUES(?,?,?,CURDATE(),?,'PENDING',?)",account,number,type,Date.valueOf(LocalDate.now().plusYears(DemoRules.integer("card.expiryYears"))),DemoRules.number("card.dailyLimit"));
            if("DEBIT".equals(type))Jdbc.exactlyOne(c,"INSERT INTO debit_card(card_id,daily_withdrawal_limit) VALUES(?,?)",id,DemoRules.number("card.withdrawalLimit"));
            else Jdbc.exactlyOne(c,"INSERT INTO credit_card(card_id,credit_limit,available_credit) VALUES(?,?,?)",id,DemoRules.number("card.creditLimit"),DemoRules.number("card.creditLimit"));
            NotificationDAO.submitted(c,NotificationDAO.Product.CARD,id);Jdbc.audit(c,null,"CARD_REQUEST","Customer "+customer+"; card "+id);return id;});
    }
    public void customerAction(int customer,int id,String action,String limit)throws SQLException {
        Input.choice(action,"block","cancel","close","limit");
        Jdbc.transaction(c->{Jdbc.customer(c,customer);var row=Jdbc.one(c,"SELECT c.status,c.card_type FROM card c JOIN account a ON a.account_number=c.account_number WHERE c.card_id=? AND a.customer_id=? FOR UPDATE",id,customer);String status=(String)row.get("status");
            if("limit".equals(action)) {if(!"ACTIVE".equals(status))throw new IllegalArgumentException("Only active card limits can change.");BigDecimal value;try{value=Input.money(new BigDecimal(limit));}catch(RuntimeException e){throw new IllegalArgumentException("Enter a valid daily limit.");}if(value.compareTo(DemoRules.number("card.dailyLimit"))>0)throw new IllegalArgumentException("Limit exceeds the demo maximum.");Jdbc.exactlyOne(c,"UPDATE card SET daily_limit=? WHERE card_id=?",value,id);}
            else if("block".equals(action)){if(!"ACTIVE".equals(status))throw new IllegalArgumentException("Only active cards can be blocked.");Jdbc.exactlyOne(c,"UPDATE card SET status='BLOCKED' WHERE card_id=?",id);}
            else {if("cancel".equals(action) && !"PENDING".equals(status) || "close".equals(action) && !java.util.List.of("ACTIVE","BLOCKED","EXPIRED").contains(status))throw new IllegalArgumentException("Card cannot be cancelled in its current state.");noDebt(c,id);Jdbc.exactlyOne(c,"UPDATE card SET status='CANCELLED' WHERE card_id=?",id);}
            if(!"limit".equals(action))NotificationDAO.changed(c,NotificationDAO.Product.CARD,id,action);Jdbc.audit(c,null,"CARD_"+action.toUpperCase(),"Customer "+customer+"; card "+id);return null;});
    }
    public void process(int employee,int id,String action,String reason)throws SQLException {
        Input.choice(action,"approve","reject","block","unblock","close");final String note=Input.text(reason,500,"reason");
        Jdbc.transaction(c->{Jdbc.staff(c,employee,"CARD_SERVICES_OFFICER");var row=Jdbc.one(c,"SELECT c.status,c.expiry_date,a.status AS account_status,u.status AS customer_status FROM card c JOIN account a ON a.account_number=c.account_number JOIN customer u ON u.customer_id=a.customer_id WHERE c.card_id=? FOR UPDATE",id);String current=(String)row.get("status");
            String next=switch(action){case "approve","unblock"->"ACTIVE";case "block"->"BLOCKED";default->"CANCELLED";};
            boolean valid=switch(action){case "approve","reject"->"PENDING".equals(current);case "block"->"ACTIVE".equals(current);case "unblock"->"BLOCKED".equals(current);default->java.util.List.of("ACTIVE","BLOCKED","EXPIRED").contains(current);};
            if(!valid)throw new IllegalArgumentException("Card action is no longer eligible.");
            if("ACTIVE".equals(next) && (!"ACTIVE".equals(row.get("account_status")) || !"ACTIVE".equals(row.get("customer_status"))))throw new IllegalArgumentException("Active account and customer required.");
            if("unblock".equals(action) && ((Date)row.get("expiry_date")).toLocalDate().isBefore(LocalDate.now()))throw new IllegalArgumentException("Expired cards cannot be reactivated.");
            if("CANCELLED".equals(next))noDebt(c,id);
            if("approve".equals(action))Jdbc.exactlyOne(c,"UPDATE card SET status=?,decision_note=?,issue_date=CURDATE(),expiry_date=? WHERE card_id=?",next,note,Date.valueOf(LocalDate.now().plusYears(DemoRules.integer("card.expiryYears"))),id);
            else Jdbc.exactlyOne(c,"UPDATE card SET status=?,decision_note=? WHERE card_id=?",next,note,id);
            NotificationDAO.changed(c,NotificationDAO.Product.CARD,id,action);Jdbc.audit(c,employee,"CARD_"+action.toUpperCase(),"Card "+id+"; "+note);return null;});
    }
    static void noDebt(Connection c,int id)throws SQLException {if(!Jdbc.rows(c,"SELECT card_id FROM credit_card WHERE card_id=? AND available_credit<credit_limit FOR UPDATE",id).isEmpty())throw new IllegalArgumentException("Outstanding card balance must be settled before closure.");}
}
