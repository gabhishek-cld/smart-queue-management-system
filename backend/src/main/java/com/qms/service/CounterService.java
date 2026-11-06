package com.qms.service;

import com.qms.exception.QueueEmptyException;
import com.qms.exception.ResourceNotFoundException;
import com.qms.model.dto.response.BoardEvent;
import com.qms.model.dto.response.TicketResponse;
import com.qms.model.entity.Counter;
import com.qms.model.entity.Ticket;
import com.qms.model.enums.CounterStatus;
import com.qms.model.enums.TicketStatus;
import com.qms.repository.CounterRepository;
import com.qms.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;
    private final TicketRepository ticketRepository;
    private final QueueService queueService;
    private final TicketService ticketService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TicketResponse callNext(Long counterId) {
        Counter counter = getActiveCounter(counterId);

        if (counter.getCurrentTicket() != null) {
            completeCurrentTicket(counter.getCurrentTicket());
        }

        Ticket next = ticketRepository.findNextTicketWithLock()
                .orElseThrow(QueueEmptyException::new);

        next.setStatus(TicketStatus.CALLED);
        next.setCalledAt(LocalDateTime.now());
        next.setCounter(counter);
        ticketRepository.save(next);

        counter.setCurrentTicket(next);
        counterRepository.save(counter);

        queueService.removeFromQueue(next);

        broadcastToBoard(next, counter);

        return ticketService.mapToResponse(next);
    }

    @Transactional
    public TicketResponse markCompleted(Long counterId) {
        Counter counter = getActiveCounter(counterId);
        Ticket ticket = requireCurrentTicket(counter);

        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setCompletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        updateRollingAvgServiceTime(ticket);

        counter.setCurrentTicket(null);
        counterRepository.save(counter);

        return ticketService.mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse markNoShow(Long counterId) {
        Counter counter = getActiveCounter(counterId);
        Ticket ticket = requireCurrentTicket(counter);

        ticket.setStatus(TicketStatus.NO_SHOW);
        ticket.setCompletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        counter.setCurrentTicket(null);
        counterRepository.save(counter);

        return ticketService.mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse transferTicket(Long counterId, Long targetServiceTypeId) {
        Counter counter = getActiveCounter(counterId);
        Ticket ticket = requireCurrentTicket(counter);

        ticket.setStatus(TicketStatus.WAITING);
        ticket.setCalledAt(null);
        ticket.setCounter(null);

        var targetServiceType = ticket.getServiceType();
        if (targetServiceTypeId != null) {
            targetServiceType = ticketRepository.findById(targetServiceTypeId)
                    .map(Ticket::getServiceType)
                    .orElse(ticket.getServiceType());
        }
        ticket.setServiceType(targetServiceType);
        ticketRepository.save(ticket);

        counter.setCurrentTicket(null);
        counterRepository.save(counter);

        queueService.addToQueue(ticket);

        return ticketService.mapToResponse(ticket);
    }

    public List<Counter> getAllCounters() {
        return counterRepository.findAll();
    }

    private void updateRollingAvgServiceTime(Ticket ticket) {
        if (ticket.getCalledAt() == null || ticket.getCompletedAt() == null) return;

        long servicedSeconds = java.time.Duration.between(ticket.getCalledAt(), ticket.getCompletedAt()).getSeconds();
        double actualMinutes = servicedSeconds / 60.0;
        double current = ticket.getServiceType().getAvgServiceMinutes();
        double updated = 0.7 * current + 0.3 * actualMinutes;
        ticket.getServiceType().setAvgServiceMinutes(updated);
    }

    private void completeCurrentTicket(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.CALLED) {
            ticket.setStatus(TicketStatus.COMPLETED);
            ticket.setCompletedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
    }

    private void broadcastToBoard(Ticket ticket, Counter counter) {
        BoardEvent event = new BoardEvent(
                ticket.getTokenNumber(),
                counter.getName(),
                ticket.getServiceType().getName(),
                ticket.getCalledAt()
        );
        messagingTemplate.convertAndSend("/topic/board", event);
    }

    private Counter getActiveCounter(Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter not found: " + counterId));
        if (counter.getStatus() == CounterStatus.OFFLINE) {
            throw new IllegalStateException("Counter " + counter.getName() + " is offline.");
        }
        return counter;
    }

    private Ticket requireCurrentTicket(Counter counter) {
        if (counter.getCurrentTicket() == null) {
            throw new IllegalStateException("Counter " + counter.getName() + " has no active ticket.");
        }
        return counter.getCurrentTicket();
    }
}
