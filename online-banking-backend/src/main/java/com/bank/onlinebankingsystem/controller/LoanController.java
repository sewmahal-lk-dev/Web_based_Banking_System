package com.bank.onlinebankingsystem.controller;

import com.bank.onlinebankingsystem.entity.Loan;
import com.bank.onlinebankingsystem.repository.LoanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;

    // 1. Admin/Staff: Get All Loans
    @GetMapping("/all")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanRepository.findAll());
    }

    // 2. Customer: Get Loans Specific to Logged User
    @GetMapping("/my-loans/{userId}")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(loanRepository.findByUserId(userId));
    }

    // 3. Customer: Apply Loan
    @PostMapping("/apply")
    public ResponseEntity<Loan> applyLoan(@RequestBody Loan loan) {
        loan.setStatus("Pending");
        Loan savedLoan = loanRepository.save(loan);
        return ResponseEntity.ok(savedLoan);
    }

    // 4. Admin/Staff: Update Status
    @PutMapping("/{id}/status")
    public ResponseEntity<Loan> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return loanRepository.findById(id).map(loan -> {
            loan.setStatus(status);
            Loan updatedLoan = loanRepository.save(loan);
            return ResponseEntity.ok(updatedLoan);
        }).orElse(ResponseEntity.notFound().build());
    }
}