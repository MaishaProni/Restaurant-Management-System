package com.restaurant.service.payment;

import com.restaurant.dto.PaymentRequest;
import com.restaurant.exception.BusinessException;
import org.springframework.stereotype.Component;

/** Strategy Pattern — concrete strategy for CARD payments. */
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public void validate(PaymentRequest request, double amount) {
        if (amount <= 0) {
            throw new BusinessException("Card payment amount must be greater than zero.");
        }
    }

    @Override
    public String getDescription() {
        return "Card payment processed via POS terminal.";
    }
}
