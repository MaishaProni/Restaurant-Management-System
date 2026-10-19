package com.restaurant.service.payment;

import com.restaurant.dto.PaymentRequest;
import com.restaurant.exception.BusinessException;
import org.springframework.stereotype.Component;

/** Strategy Pattern — concrete strategy for MOBILE payments. */
@Component
public class MobilePaymentStrategy implements PaymentStrategy {

    @Override
    public void validate(PaymentRequest request, double amount) {
        if (amount <= 0) {
            throw new BusinessException("Mobile payment amount must be greater than zero.");
        }
    }

    @Override
    public String getDescription() {
        return "Mobile payment processed via QR/digital wallet.";
    }
}
