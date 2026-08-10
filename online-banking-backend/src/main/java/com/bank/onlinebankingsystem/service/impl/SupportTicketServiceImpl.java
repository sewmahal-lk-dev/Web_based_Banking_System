package com.bank.onlinebankingsystem.service.impl;

import com.bank.onlinebankingsystem.entity.SupportTicket;
import com.bank.onlinebankingsystem.repository.SupportTicketRepository;
import com.bank.onlinebankingsystem.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Override
    public SupportTicket createTicket(SupportTicket ticket) {
        return supportTicketRepository.save(ticket);
    }

    @Override
    public List<SupportTicket> getTicketsByUserId(Long userId) {
        return supportTicketRepository.findByUserId(userId);
    }
}