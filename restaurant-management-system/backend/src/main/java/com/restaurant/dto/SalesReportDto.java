package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesReportDto {
    private String startDate;
    private String endDate;
    private int totalOrders;
    private double totalRevenue;
}
