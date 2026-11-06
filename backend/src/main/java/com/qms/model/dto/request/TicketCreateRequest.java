package com.qms.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotNull(message = "Service type is required")
    private Long serviceTypeId;

    private String customerName;
}
