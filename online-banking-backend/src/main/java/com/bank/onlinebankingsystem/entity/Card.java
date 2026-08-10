package com.bank.onlinebankingsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // එකතු කළා
    private String holder;
    private String cardType;
    private String fullCardNumber;
    private String cvv;
    private String expiry;
    private Double limitAmount;
    private String status; // 'Active', 'Blocked', 'Pending Approval'

    private String pendingReq;  // 'Block Requested', 'Unblock Requested', 'None'
    private String blockReason; // Customer දාන හේතුව

    // Default Constructor
    public Card() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getFullCardNumber() { return fullCardNumber; }
    public void setFullCardNumber(String fullCardNumber) { this.fullCardNumber = fullCardNumber; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public Double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(Double limitAmount) { this.limitAmount = limitAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPendingReq() { return pendingReq; }
    public void setPendingReq(String pendingReq) { this.pendingReq = pendingReq; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
}