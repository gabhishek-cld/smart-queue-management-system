package com.qms.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CounterRequest {

    @NotBlank(message = "Counter name is required")
    private String name;

    private String staffName;
}
