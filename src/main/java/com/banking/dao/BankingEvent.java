package com.banking.dao;

import java.util.Objects;

/** Internal business events. IDs come from verified writes, never notification recipients from forms. */
public sealed interface BankingEvent {
    enum Product {
        LOAN("loan", "loan_id", "Loan", "LOAN_OFFICER"),
        INVESTMENT("investment", "investment_id", "Investment", "INVESTMENT_OFFICER"),
        CARD("card", "card_id", "Card", "CARD_SERVICES_OFFICER"),
        REQUEST("service_request", "request_id", "Service request", "CUSTOMER_SERVICE_OFFICER");
        final String table,key,label,role;
        Product(String table,String key,String label,String role){this.table=table;this.key=key;this.label=label;this.role=role;}
    }
    record ProductSubmitted(Product product,long id) implements BankingEvent {
        public ProductSubmitted {Objects.requireNonNull(product);}
    }
    record ProductChanged(Product product,long id,String action) implements BankingEvent {
        public ProductChanged {Objects.requireNonNull(product);}
        public ProductChanged(Product product,long id){this(product,id,null);}
    }
    record AccountChanged(long number) implements BankingEvent {}
    record ProfileChanged(int customer) implements BankingEvent {}
    record CustomerRegistered(int customer) implements BankingEvent {}
    record EmployeeAccessChanged(int employee,int actor) implements BankingEvent {}
    record PaymentRecorded(String reference) implements BankingEvent {
        public PaymentRecorded {Objects.requireNonNull(reference);}
    }
    record PaymentChanged(long id) implements BankingEvent {}
    record CustomerReplied(int ticket) implements BankingEvent {}
    // These preserve existing support messages and notification kinds without inventing new notices.
    record TicketCustomerNotice(int ticket,String type,String title,String message) implements BankingEvent {}
    record TicketDepartmentNotice(int ticket,String role,Integer specific,Integer actor,
            String type,String title,String message) implements BankingEvent {}
}
