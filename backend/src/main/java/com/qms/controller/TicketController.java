package com.qms.controller;

import com.qms.model.dto.request.TicketCreateRequest;
import com.qms.model.dto.response.TicketResponse;
import com.qms.model.entity.ServiceType;
import com.qms.repository.ServiceTypeRepository;
import com.qms.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Customer / Kiosk")
public class TicketController {

    private final TicketService ticketService;
    private final ServiceTypeRepository serviceTypeRepository;

    @GetMapping("/services")
    @Operation(summary = "List available service types for the kiosk")
    public ResponseEntity<List<ServiceType>> getServices() {
        return ResponseEntity.ok(serviceTypeRepository.findAll());
    }

    @PostMapping("/tickets")
    @Operation(summary = "Generate a new ticket for a selected service")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    @GetMapping("/tickets/{id}")
    @Operation(summary = "Check status and ETA for a specific ticket")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketStatus(id));
    }
}
