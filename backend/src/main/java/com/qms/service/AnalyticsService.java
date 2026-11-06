package com.qms.service;

import com.qms.model.dto.response.AnalyticsResponse;
import com.qms.model.entity.Ticket;
import com.qms.model.enums.TicketStatus;
import com.qms.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TicketRepository ticketRepository;

    public AnalyticsResponse getTodayAnalytics() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> todayTickets = ticketRepository.findByCreatedAtBetween(startOfDay, now);

        long completed = count(todayTickets, TicketStatus.COMPLETED);
        long noShow = count(todayTickets, TicketStatus.NO_SHOW);

        double avgWait = todayTickets.stream()
                .filter(t -> t.getCalledAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCalledAt()).getSeconds())
                .average()
                .orElse(0) / 60.0;

        double avgService = todayTickets.stream()
                .filter(t -> t.getCalledAt() != null && t.getCompletedAt() != null)
                .mapToLong(t -> Duration.between(t.getCalledAt(), t.getCompletedAt()).getSeconds())
                .average()
                .orElse(0) / 60.0;

        Map<String, Long> byServiceType = todayTickets.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getServiceType().getName(),
                        Collectors.counting()
                ));

        Map<Integer, Long> byHour = todayTickets.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().getHour(),
                        Collectors.counting()
                ));

        return AnalyticsResponse.builder()
                .totalTicketsToday(todayTickets.size())
                .totalCompleted(completed)
                .totalNoShow(noShow)
                .avgWaitMinutes(Math.round(avgWait * 10.0) / 10.0)
                .avgServiceMinutes(Math.round(avgService * 10.0) / 10.0)
                .ticketsByServiceType(byServiceType)
                .ticketsByHour(byHour)
                .build();
    }

    private long count(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream().filter(t -> t.getStatus() == status).count();
    }
}
