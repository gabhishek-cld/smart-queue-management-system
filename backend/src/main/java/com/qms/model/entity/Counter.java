package com.qms.model.entity;

import com.qms.model.enums.CounterStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "counters")
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;                   // e.g., "Counter 1"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CounterStatus status = CounterStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_ticket_id")
    private Ticket currentTicket;          // The ticket currently being served (null if idle)

    @Column(name = "staff_name")
    private String staffName;              // Which staff member is at this counter
}
