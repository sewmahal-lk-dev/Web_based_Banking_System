package com.banking.dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Stateless reactions for active staff/departments, preserving actor exclusions. */
public final class StaffNotificationObserver implements BankingEventObserver {
    @Override public void onEvent(Connection c,BankingEvent event)throws SQLException {
        if(event instanceof BankingEvent.ProductSubmitted e)NotificationDAO.submitted(c,e.product(),e.id());
        else if(event instanceof BankingEvent.AccountChanged e)NotificationDAO.accountStaffChanged(c,e.number());
        else if(event instanceof BankingEvent.CustomerRegistered e)NotificationDAO.registered(c,e.customer());
        else if(event instanceof BankingEvent.EmployeeAccessChanged e)NotificationDAO.employeeAccess(c,e.employee(),e.actor());
        else if(event instanceof BankingEvent.CustomerReplied e)NotificationDAO.customerReply(c,e.ticket());
        else if(event instanceof BankingEvent.TicketDepartmentNotice e)NotificationDAO.department(c,e.ticket(),e.role(),e.specific(),e.actor(),e.type(),e.title(),e.message());
    }
}
