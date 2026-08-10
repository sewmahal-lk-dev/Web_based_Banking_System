package com.bank.onlinebankingsystem.service.impl;

import com.bank.onlinebankingsystem.entity.Investment;
import com.bank.onlinebankingsystem.repository.InvestmentRepository;
import com.bank.onlinebankingsystem.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    @Autowired
    private InvestmentRepository investmentRepository;

    @Override
    public Investment createInvestment(Investment investment) {
        return investmentRepository.save(investment);
    }

    @Override
    public List<Investment> getInvestmentsByUserId(Long userId) {
        return investmentRepository.findByUserId(userId);
    }
}