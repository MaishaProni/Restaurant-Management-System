package com.restaurant.service;

import com.restaurant.dto.SalesReportDto;
import com.restaurant.dto.TopItemDto;
import com.restaurant.model.Payment;
import com.restaurant.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentRepository paymentRepo;

    public SalesReportDto getSalesReport(LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);

        List<Payment> payments = paymentRepo.findByPaidAtBetween(startDt, endDt);

        double revenue = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        return new SalesReportDto(start.toString(), end.toString(), payments.size(),
                Math.round(revenue * 100.0) / 100.0);
    }

    public List<TopItemDto> getTopSellingItems(LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);

        return paymentRepo.findTopSellingItemsByPaidAtBetween(startDt, endDt).stream()
                .map(row -> new TopItemDto((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }
}
