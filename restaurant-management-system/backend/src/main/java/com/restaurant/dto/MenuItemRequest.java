package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    private Integer categoryId;

    private Boolean isAvailable = true;
}
