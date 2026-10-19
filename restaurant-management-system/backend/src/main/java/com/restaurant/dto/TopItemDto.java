package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopItemDto {
    private String itemName;
    private long quantitySold;
}
