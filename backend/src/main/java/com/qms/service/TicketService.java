package com.qms.service;

import com.qms.exception.ResourceNotFoundException;
import com.qms.model.dto.request.TicketCreateRequest;
import com.qms.model.dto.response.TicketResponse;
import com.qms.model.entity.ServiceType;
import com.qms.model.entity.Ticket;
import com.qms.model.enums.TicketStatus;
import com.qms.repository.ServiceTypeRepository;
import com.qms.repository.TicketRepository;
import com.qms.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final QueueService queueService;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request) {
        ServiceType serviceType = serviceTypeRepository.findById(request.getServiceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Service type not found"));

        long sequence = ticketRepository.countByServiceTypeId(serviceType.getId()) + 1;
        String tokenNumber = tokenGenerator.generate(serviceType.getPrefix(), sequence);

        Ticket ticket = new Ticket();
        ticket.setTokenNumber(tokenNumber);
        ticket.setServiceType(serviceType);
        ticket.setCustomerName(request.getCustomerName());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.WAITING);

        Ticket saved = ticketRepository.save(ticket);
        queueService.addToQueue(saved);

        return mapToResponse(saved);
    }

    public TicketResponse getTicketStatus(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        return mapToResponse(ticket);
    }

    public TicketResponse mapToResponse(Ticket ticket) {
        int position = 0;
        double eta = 0;

        if (ticket.getStatus() == TicketStatus.WAITING) {
            position = queueService.getPositionInQueue(ticket);
            eta = queueService.calculateEta(ticket);
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .tokenNumber(ticket.getTokenNumber())
                .serviceTypeName(ticket.getServiceType().getName())
                .status(ticket.getStatus())
                .counterName(ticket.getCounter() != null ? ticket.getCounter().getName() : null)
                .customerName(ticket.getCustomerName())
                .createdAt(ticket.getCreatedAt())
                .calledAt(ticket.getCalledAt())
                .completedAt(ticket.getCompletedAt())
                .positionInQueue(position)
                .estimatedWaitMinutes(eta)
                .build();
    }
}
