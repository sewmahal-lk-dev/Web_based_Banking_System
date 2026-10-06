package com.banking.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Stateless reactions for customer-owned notifications. */
public final class CustomerNotificationObserver implements BankingEventObserver {
    @Override public void onEvent(Connection c,BankingEvent event)throws SQLException {
        if(event instanceof BankingEvent.ProductChanged e)NotificationDAO.changed(c,e.product(),e.id(),e.action());
        else if(event instanceof BankingEvent.AccountChanged e)NotificationDAO.accountChanged(c,e.number());
        else if(event instanceof BankingEvent.ProfileChanged e)NotificationDAO.profileChanged(c,e.customer());
        else if(event instanceof BankingEvent.PaymentRecorded e)NotificationDAO.payment(c,e.reference());
        else if(event instanceof BankingEvent.PaymentChanged e)NotificationDAO.paymentId(c,e.id());
        else if(event instanceof BankingEvent.TicketCustomerNotice e)NotificationDAO.customer(c,e.ticket(),e.type(),e.title(),e.message());
    }
}
