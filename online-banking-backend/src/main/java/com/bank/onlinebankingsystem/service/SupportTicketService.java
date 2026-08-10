package com.bank.onlinebankingsystem.service;

import com.bank.onlinebankingsystem.entity.SupportTicket;
import java.util.List;

public interface SupportTicketService {
    SupportTicket createTicket(SupportTicket ticket);
    List<SupportTicket> getTicketsByUserId(Long userId);
}