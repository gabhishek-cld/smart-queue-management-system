package com.qms.repository;

import com.qms.model.entity.Ticket;
import com.qms.model.enums.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);

    List<Ticket> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Count tokens issued for a specific service type, used to generate sequential token numbers
    long countByServiceTypeId(Long serviceTypeId);

    // The DB row is locked until our transaction commits, so the second counter gets the next one.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM Ticket t
        WHERE t.status = 'WAITING'
        ORDER BY t.serviceType.priorityWeight DESC, t.createdAt ASC
        LIMIT 1
    """)
    Optional<Ticket> findNextTicketWithLock();

    // restricted to a specific service type, used for counter transfer
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM Ticket t
        WHERE t.status = 'WAITING' AND t.serviceType.id = :serviceTypeId
        ORDER BY t.createdAt ASC
        LIMIT 1
    """)
    Optional<Ticket> findNextTicketByServiceTypeWithLock(Long serviceTypeId);
}
