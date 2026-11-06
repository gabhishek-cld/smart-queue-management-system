package com.qms.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BoardEvent {
    private String tokenNumber;
    private String counterName;
    private String serviceTypeName;
    private LocalDateTime calledAt;
}
