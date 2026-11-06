package com.qms.service;

import com.qms.model.entity.Ticket;
import com.qms.model.enums.TicketStatus;
import com.qms.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final TicketRepository ticketRepository;

    private final PriorityBlockingQueue<Ticket> waitingQueue = new PriorityBlockingQueue<>(
            100,
            Comparator.comparingInt((Ticket t) -> -t.getServiceType().getPriorityWeight())
                    .thenComparing(Ticket::getCreatedAt)
    );

    @PostConstruct
    public void loadQueueFromDatabase() {
        List<Ticket> waiting = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.WAITING);
        waitingQueue.addAll(waiting);
    }

    public void addToQueue(Ticket ticket) {
        waitingQueue.add(ticket);
    }

    public void removeFromQueue(Ticket ticket) {
        waitingQueue.remove(ticket);
    }

    public int getQueueSize() {
        return waitingQueue.size();
    }

    public int getPositionInQueue(Ticket ticket) {
        return (int) waitingQueue.stream()
                .filter(t -> !t.getId().equals(ticket.getId()))
                .filter(t -> isAheadOf(t, ticket))
                .count();
    }

    public double calculateEta(Ticket ticket) {
        int position = getPositionInQueue(ticket);
        double avgMinutes = ticket.getServiceType().getAvgServiceMinutes();
        return position * avgMinutes;
    }

    private boolean isAheadOf(Ticket candidate, Ticket reference) {
        int pw1 = candidate.getServiceType().getPriorityWeight();
        int pw2 = reference.getServiceType().getPriorityWeight();
        if (pw1 != pw2) return pw1 > pw2;
        return candidate.getCreatedAt().isBefore(reference.getCreatedAt());
    }
}
