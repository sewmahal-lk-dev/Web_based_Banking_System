package com.bank.onlinebankingsystem.controller;

import com.bank.onlinebankingsystem.entity.Investment;
import com.bank.onlinebankingsystem.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@CrossOrigin(origins = "*")
public class InvestmentController {

    @Autowired
    private InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<Investment> createInvestment(@RequestBody Investment investment) {
        return ResponseEntity.ok(investmentService.createInvestment(investment));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Investment>> getInvestments(@PathVariable Long userId) {
        return ResponseEntity.ok(investmentService.getInvestmentsByUserId(userId));
    }
}