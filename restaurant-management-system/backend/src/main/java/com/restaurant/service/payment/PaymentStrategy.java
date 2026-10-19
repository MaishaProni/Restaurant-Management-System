package com.restaurant.service.payment;

import com.restaurant.dto.PaymentRequest;

/**
 * Strategy Pattern — defines the interface for all payment strategies.
 * Each payment method (CASH, CARD, MOBILE) is a concrete strategy.
 */
public interface PaymentStrategy {
    void validate(PaymentRequest request, double amount);
    String getDescription();
}
