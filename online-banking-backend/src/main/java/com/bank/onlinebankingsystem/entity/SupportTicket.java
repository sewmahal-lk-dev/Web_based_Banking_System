package com.bank.onlinebankingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(nullable = false)
    private Long userId;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status = "OPEN";
}