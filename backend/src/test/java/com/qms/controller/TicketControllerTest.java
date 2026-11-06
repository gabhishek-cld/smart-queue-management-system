package com.qms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qms.model.dto.request.TicketCreateRequest;
import com.qms.model.dto.response.TicketResponse;
import com.qms.model.enums.TicketStatus;
import com.qms.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Test
    @DisplayName("POST /api/tickets returns 201 with a token number")
    void createTicketReturns201() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setServiceTypeId(1L);

        TicketResponse mockResponse = TicketResponse.builder()
                .id(1L)
                .tokenNumber("C-001")
                .serviceTypeName("Cash Deposit")
                .status(TicketStatus.WAITING)
                .positionInQueue(0)
                .estimatedWaitMinutes(5.0)
                .createdAt(LocalDateTime.now())
                .build();

        when(ticketService.createTicket(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenNumber").value("C-001"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @DisplayName("POST /api/tickets without serviceTypeId returns 400")
    void createTicketWithMissingFieldReturns400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
