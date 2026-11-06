package com.qms.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private long totalTicketsToday;
    private long totalCompleted;
    private long totalNoShow;
    private double avgWaitMinutes;
    private double avgServiceMinutes;
    private Map<String, Long> ticketsByServiceType;
    private Map<Integer, Long> ticketsByHour;
}
