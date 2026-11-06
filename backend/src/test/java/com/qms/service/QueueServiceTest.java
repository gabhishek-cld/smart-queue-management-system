package com.qms.service;

import com.qms.model.entity.ServiceType;
import com.qms.model.entity.Ticket;
import com.qms.model.enums.TicketStatus;
import com.qms.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Collections.emptyList());
        queueService.loadQueueFromDatabase();
    }

    @Test
    @DisplayName("VIP ticket should be at position 0 even if added after a normal ticket")
    void vipTicketIsAheadOfNormalTicket() {
        Ticket normal = makeTicket(1L, "C-001", 2, LocalDateTime.now().minusMinutes(5));
        Ticket vip    = makeTicket(2L, "V-001", 10, LocalDateTime.now());

        queueService.addToQueue(normal);
        queueService.addToQueue(vip);

        // VIP has higher priority weight (10 > 2), so it should be at position 0
        assertThat(queueService.getPositionInQueue(vip)).isEqualTo(0);
        assertThat(queueService.getPositionInQueue(normal)).isEqualTo(1);
    }

    @Test
    @DisplayName("Among same-priority tickets, earlier arrival should be served first")
    void earlierTicketIsAheadOfLaterTicket() {
        Ticket early = makeTicket(1L, "C-001", 2, LocalDateTime.now().minusMinutes(10));
        Ticket late  = makeTicket(2L, "C-002", 2, LocalDateTime.now());

        queueService.addToQueue(early);
        queueService.addToQueue(late);

        assertThat(queueService.getPositionInQueue(early)).isEqualTo(0);
        assertThat(queueService.getPositionInQueue(late)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETA = position × avgServiceMinutes for the ticket's service type")
    void etaCalculationIsCorrect() {
        Ticket first  = makeTicket(1L, "A-001", 2, LocalDateTime.now().minusMinutes(10));
        Ticket second = makeTicket(2L, "A-002", 2, LocalDateTime.now());
        // avgServiceMinutes = 8.0 for both

        queueService.addToQueue(first);
        queueService.addToQueue(second);

        // second is at position 1, so ETA = 1 × 8.0 = 8.0 minutes
        assertThat(queueService.calculateEta(second)).isEqualTo(8.0);
        // first is at position 0, so ETA = 0
        assertThat(queueService.calculateEta(first)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Queue size should decrease when a ticket is removed")
    void removeFromQueueDecreasesSize() {
        Ticket t = makeTicket(1L, "C-001", 2, LocalDateTime.now());
        queueService.addToQueue(t);
        assertThat(queueService.getQueueSize()).isEqualTo(1);

        queueService.removeFromQueue(t);
        assertThat(queueService.getQueueSize()).isEqualTo(0);
    }

    // builds a lightweight Ticket with just enough fields for queue logic
    private Ticket makeTicket(Long id, String token, int priorityWeight, LocalDateTime createdAt) {
        ServiceType st = new ServiceType();
        st.setPriorityWeight(priorityWeight);
        st.setAvgServiceMinutes(8.0);

        Ticket t = new Ticket();
        t.setId(id);
        t.setTokenNumber(token);
        t.setServiceType(st);
        t.setStatus(TicketStatus.WAITING);
        t.setCreatedAt(createdAt);
        return t;
    }
}
