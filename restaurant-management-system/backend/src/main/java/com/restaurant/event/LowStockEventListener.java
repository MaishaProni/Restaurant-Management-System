package com.restaurant.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Listener/subscriber for LowStockEvent.
 * Logs the alert; in production this could send push notifications or emails.
 */
@Component
public class LowStockEventListener {

    private static final Logger log = LoggerFactory.getLogger(LowStockEventListener.class);

    @EventListener
    public void handleLowStockAlert(LowStockEvent event) {
        var ingredient = event.getIngredient();
        log.warn("[LOW STOCK ALERT] Ingredient '{}' — current stock: {} {}, threshold: {} {}",
                ingredient.getName(),
                ingredient.getCurrentStock(), ingredient.getUnit(),
                ingredient.getLowStockThreshold(), ingredient.getUnit());
    }
}
