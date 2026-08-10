package com.bank.onlinebankingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long investmentId;

    @Column(nullable = false)
    private Long userId;

    private String type; // FIXED_DEPOSIT, SAVINGS_PLAN
    private Double amount;
    private Integer durationMonths;
}