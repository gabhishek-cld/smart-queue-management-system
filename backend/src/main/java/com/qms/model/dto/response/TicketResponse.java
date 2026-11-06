package com.qms.model.dto.response;

import com.qms.model.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String tokenNumber;
    private String serviceTypeName;
    private TicketStatus status;
    private String counterName;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
    private int positionInQueue;
    private double estimatedWaitMinutes;
}
