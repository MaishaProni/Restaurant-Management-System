package com.restaurant.dto;

import com.restaurant.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull
    private PaymentMethod method;
}
