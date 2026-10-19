package com.restaurant.service;

import com.restaurant.dto.AdjustmentRequest;
import com.restaurant.dto.IngredientRequest;
import com.restaurant.event.LowStockEvent;
import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.*;
import com.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final IngredientRepository ingredientRepo;
    private final InventoryAdjustmentRepository adjustmentRepo;
    private final UserRepository userRepo;
    private final RecipeItemRepository recipeItemRepo;

    /** Observer Pattern — publisher informs all registered listeners. */
    private final ApplicationEventPublisher eventPublisher;

    public List<Ingredient> getAllIngredients() {
        return ingredientRepo.findAll();
    }

    public List<Ingredient> getLowStockIngredients() {
        return ingredientRepo.findLowStockIngredients();
    }

    public Ingredient createIngredient(IngredientRequest req) {
        Ingredient ing = new Ingredient();
        ing.setName(req.getName());
        ing.setCurrentStock(req.getCurrentStock());
        ing.setUnit(req.getUnit());
        ing.setLowStockThreshold(req.getLowStockThreshold() != null ? req.getLowStockThreshold() : 0.0);
        return ingredientRepo.save(ing);
    }

    public Ingredient updateIngredient(Integer id, IngredientRequest req) {
        Ingredient ing = ingredientRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        ing.setName(req.getName());
        ing.setCurrentStock(req.getCurrentStock());
        ing.setUnit(req.getUnit());
        ing.setLowStockThreshold(req.getLowStockThreshold() != null ? req.getLowStockThreshold() : 0.0);
        return ingredientRepo.save(ing);
    }

    @Transactional
    public Ingredient adjustStock(Integer ingredientId, Integer managerId, AdjustmentRequest req) {
        Ingredient ing = ingredientRepo.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        double newStock = ing.getCurrentStock() + req.getQuantityChanged();
        if (newStock < 0) {
            throw new BusinessException("Adjustment would reduce stock below zero. Current: "
                    + ing.getCurrentStock() + ", Change: " + req.getQuantityChanged());
        }

        ing.setCurrentStock(newStock);
        ingredientRepo.save(ing);

        InventoryAdjustment log = new InventoryAdjustment();
        log.setIngredient(ing);
        log.setManager(manager);
        log.setQuantityChanged(req.getQuantityChanged());
        log.setReason(req.getReason());
        adjustmentRepo.save(log);

        if (ing.isLowStock()) {
            // Observer Pattern — publish event; listener logs / notifies
            eventPublisher.publishEvent(new LowStockEvent(this, ing));
        }

        return ing;
    }

    /** Called within the payment transaction to deduct ingredient quantities (FR-19). */
    @Transactional
    public void deductIngredients(Order order) {
        for (OrderItem orderItem : order.getItems()) {
            List<RecipeItem> recipe = recipeItemRepo.findByMenuItem(orderItem.getMenuItem());
            for (RecipeItem ri : recipe) {
                Ingredient ing = ri.getIngredient();
                double deduct = ri.getQuantityRequired() * orderItem.getQuantity();
                double newStock = ing.getCurrentStock() - deduct;

                if (newStock < 0) {
                    throw new BusinessException(
                            "Cannot deduct: stock for '" + ing.getName() + "' would go below zero.");
                }

                ing.setCurrentStock(newStock);
                ingredientRepo.save(ing);

                if (ing.isLowStock()) {
                    eventPublisher.publishEvent(new LowStockEvent(this, ing));
                }
            }
        }
    }

    public List<InventoryAdjustment> getAdjustmentLog(Integer ingredientId) {
        return adjustmentRepo.findByIngredient_IngredientIdOrderByAdjustedAtDesc(ingredientId);
    }
}
