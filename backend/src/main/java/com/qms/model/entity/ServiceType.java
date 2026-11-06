package com.qms.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "service_types")
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;                   // e.g., "Cash Deposit"

    @Column(nullable = false, length = 3)
    private String prefix;                 // e.g., "C" → generates tokens like C-001

    @Column(name = "priority_weight", nullable = false)
    private Integer priorityWeight;        // Higher = served first (VIP = 10, Normal = 1)

    @Column(name = "avg_service_minutes", nullable = false)
    private Double avgServiceMinutes;      // Rolling average used for ETA calculation
}
