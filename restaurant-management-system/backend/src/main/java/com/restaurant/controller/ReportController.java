package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.SalesReportDto;
import com.restaurant.dto.TopItemDto;
import com.restaurant.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesReportDto>> getSales(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return ResponseEntity.ok(ApiResponse.ok(reportService.getSalesReport(startDate, endDate)));
    }

    @GetMapping("/top-items")
    public ResponseEntity<ApiResponse<List<TopItemDto>>> getTopItems(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return ResponseEntity.ok(ApiResponse.ok(reportService.getTopSellingItems(startDate, endDate)));
    }
}
