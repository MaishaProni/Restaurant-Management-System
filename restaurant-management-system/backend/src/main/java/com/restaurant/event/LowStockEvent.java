package com.restaurant.event;

import com.restaurant.model.Ingredient;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern — Event published when an ingredient's stock drops
 * at or below its configured low-stock threshold.
 */
public class LowStockEvent extends ApplicationEvent {

    private final Ingredient ingredient;

    public LowStockEvent(Object source, Ingredient ingredient) {
        super(source);
        this.ingredient = ingredient;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }
}
