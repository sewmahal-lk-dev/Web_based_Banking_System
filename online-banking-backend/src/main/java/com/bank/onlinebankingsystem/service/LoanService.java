package com.bank.onlinebankingsystem.service;

import com.bank.onlinebankingsystem.entity.Loan;
import java.util.List;

public interface LoanService {
    Loan applyForLoan(Loan loan);
    List<Loan> getLoansByUserId(Long userId);
    List<Loan> getAllLoans();
    Loan updateLoanStatus(Long loanId, String status);
}