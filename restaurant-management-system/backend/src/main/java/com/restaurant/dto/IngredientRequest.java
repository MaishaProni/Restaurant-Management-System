package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngredientRequest {
    @NotBlank
    private String name;

    @NotNull
    private Double currentStock;

    @NotBlank
    private String unit;

    private Double lowStockThreshold = 0.0;
}
