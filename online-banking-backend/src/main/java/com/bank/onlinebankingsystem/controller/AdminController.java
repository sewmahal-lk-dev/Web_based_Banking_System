package com.bank.onlinebankingsystem.controller;

import com.bank.onlinebankingsystem.entity.*;
import com.bank.onlinebankingsystem.repository.*;
import com.bank.onlinebankingsystem.service.AdminService;
import com.bank.onlinebankingsystem.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanService loanService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    // 1. Admin Registration & Profile
    @PostMapping("/register")
    public ResponseEntity<Admin> registerAdmin(@RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.registerAdmin(admin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    // 2. Users Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 3. Loans Management
    @GetMapping("/loans")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanRepository.findAll());
    }

    @PutMapping("/loans/{loanId}/status")
    public ResponseEntity<Loan> updateLoanStatus(@PathVariable Long loanId, @RequestParam String status) {
        return ResponseEntity.ok(loanService.updateLoanStatus(loanId, status));
    }

    // 4. Cards Management
    @GetMapping("/cards")
    public ResponseEntity<List<Card>> getAllCards() {
        return ResponseEntity.ok(cardRepository.findAll());
    }

    @PutMapping("/cards/{cardId}/status")
    public ResponseEntity<Card> updateCardStatus(@PathVariable Long cardId, @RequestParam String status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(status);
        return ResponseEntity.ok(cardRepository.save(card));
    }

    // 5. Investments Management
    @GetMapping("/investments")
    public ResponseEntity<List<Investment>> getAllInvestments() {
        return ResponseEntity.ok(investmentRepository.findAll());
    }

    // 6. Service Requests Management
    @GetMapping("/service-requests")
    public ResponseEntity<List<ServiceRequest>> getAllServiceRequests() {
        return ResponseEntity.ok(serviceRequestRepository.findAll());
    }

    @PutMapping("/service-requests/{requestId}/status")
    public ResponseEntity<ServiceRequest> updateServiceRequestStatus(@PathVariable Long requestId, @RequestParam String status) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        return ResponseEntity.ok(serviceRequestRepository.save(request));
    }

    // 7. Support Tickets Management
    @GetMapping("/support-tickets")
    public ResponseEntity<List<SupportTicket>> getAllSupportTickets() {
        return ResponseEntity.ok(supportTicketRepository.findAll());
    }

    @PutMapping("/support-tickets/{ticketId}/status")
    public ResponseEntity<SupportTicket> updateTicketStatus(@PathVariable Long ticketId, @RequestParam String status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        return ResponseEntity.ok(supportTicketRepository.save(ticket));
    }
}