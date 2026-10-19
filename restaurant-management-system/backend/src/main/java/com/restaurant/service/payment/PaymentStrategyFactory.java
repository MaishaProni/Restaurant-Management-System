package com.restaurant.service.payment;

import com.restaurant.exception.BusinessException;
import com.restaurant.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern — resolves the correct PaymentStrategy at runtime
 * based on the chosen PaymentMethod enum value.
 */
@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final CashPaymentStrategy cashStrategy;
    private final CardPaymentStrategy cardStrategy;
    private final MobilePaymentStrategy mobileStrategy;

    public PaymentStrategy getStrategy(PaymentMethod method) {
        return switch (method) {
            case CASH   -> cashStrategy;
            case CARD   -> cardStrategy;
            case MOBILE -> mobileStrategy;
            default     -> throw new BusinessException("Unsupported payment method: " + method);
        };
    }
}
