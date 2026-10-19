package com.restaurant.service.payment;

import com.restaurant.dto.PaymentRequest;
import com.restaurant.exception.BusinessException;
import org.springframework.stereotype.Component;

/** Strategy Pattern — concrete strategy for CASH payments. */
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public void validate(PaymentRequest request, double amount) {
        if (amount <= 0) {
            throw new BusinessException("Payment amount must be greater than zero.");
        }
    }

    @Override
    public String getDescription() {
        return "Cash payment processed at counter.";
    }
}
