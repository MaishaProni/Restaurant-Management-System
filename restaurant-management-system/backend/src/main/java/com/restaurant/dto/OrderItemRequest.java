package com.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Integer menuItemId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String notes;
}
