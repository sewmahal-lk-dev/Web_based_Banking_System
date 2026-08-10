package com.bank.onlinebankingsystem.service;

import com.bank.onlinebankingsystem.entity.Investment;
import java.util.List;

public interface InvestmentService {
    Investment createInvestment(Investment investment);
    List<Investment> getInvestmentsByUserId(Long userId);
}