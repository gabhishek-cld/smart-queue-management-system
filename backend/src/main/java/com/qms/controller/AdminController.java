package com.qms.controller;

import com.qms.model.dto.request.CounterRequest;
import com.qms.model.dto.response.AnalyticsResponse;
import com.qms.model.entity.Counter;
import com.qms.model.enums.CounterStatus;
import com.qms.repository.CounterRepository;
import com.qms.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final CounterRepository counterRepository;
    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    @Operation(summary = "Today's session analytics — totals, averages, busiest hours")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getTodayAnalytics());
    }

    @PostMapping("/counters")
    @Operation(summary = "Add a new service counter")
    public ResponseEntity<Counter> addCounter(@Valid @RequestBody CounterRequest request) {
        Counter counter = new Counter();
        counter.setName(request.getName());
        counter.setStaffName(request.getStaffName());
        counter.setStatus(CounterStatus.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED).body(counterRepository.save(counter));
    }

    @PutMapping("/counters/{id}/status")
    @Operation(summary = "Set a counter's status — ACTIVE, PAUSED, or OFFLINE")
    public ResponseEntity<Counter> setCounterStatus(@PathVariable Long id,
                                                     @RequestParam CounterStatus status) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new com.qms.exception.ResourceNotFoundException("Counter not found"));
        counter.setStatus(status);
        return ResponseEntity.ok(counterRepository.save(counter));
    }

    @DeleteMapping("/counters/{id}")
    @Operation(summary = "Remove a counter")
    public ResponseEntity<Void> deleteCounter(@PathVariable Long id) {
        counterRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
