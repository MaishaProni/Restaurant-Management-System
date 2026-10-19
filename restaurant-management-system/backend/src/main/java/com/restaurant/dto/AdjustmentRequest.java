package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustmentRequest {
    @NotNull
    private Double quantityChanged;

    @NotBlank
    private String reason;
}
