package com.bank.onlinebankingsystem.controller;

import com.bank.onlinebankingsystem.entity.SupportTicket;
import com.bank.onlinebankingsystem.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support-tickets")
@CrossOrigin(origins = "*")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        return ResponseEntity.ok(supportTicketService.createTicket(ticket));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SupportTicket>> getTickets(@PathVariable Long userId) {
        return ResponseEntity.ok(supportTicketService.getTicketsByUserId(userId));
    }
}