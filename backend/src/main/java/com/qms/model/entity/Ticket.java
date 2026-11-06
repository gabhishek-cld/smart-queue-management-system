package com.qms.model.entity;

import com.qms.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, unique = true)
    private String tokenNumber;            // e.g., "C-042"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private Counter counter;               // Set when ticket is CALLED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.WAITING;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;        // When counter pressed "Next"

    @Column(name = "completed_at")
    private LocalDateTime completedAt;     // When service finished
}
