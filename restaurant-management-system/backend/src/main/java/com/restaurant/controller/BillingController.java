package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.PaymentRequest;
import com.restaurant.model.Bill;
import com.restaurant.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/orders/{orderId}/bill")
    public ResponseEntity<ApiResponse<Bill>> getBill(@PathVariable Integer orderId) {
        return ResponseEntity.ok(ApiResponse.ok(billingService.generateBill(orderId)));
    }

    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<ApiResponse<String>> processPayment(
            @PathVariable Integer orderId,
            @Valid @RequestBody PaymentRequest req) {
        String receipt = billingService.processPayment(orderId, req);
        return ResponseEntity.ok(ApiResponse.ok("Payment processed successfully", receipt));
    }

    @GetMapping("/orders/{orderId}/receipt")
    public ResponseEntity<ApiResponse<String>> getReceipt(@PathVariable Integer orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Receipt loaded", billingService.getReceipt(orderId)));
    }
}
