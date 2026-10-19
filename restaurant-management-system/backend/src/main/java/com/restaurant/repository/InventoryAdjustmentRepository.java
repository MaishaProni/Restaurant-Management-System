package com.restaurant.repository;

import com.restaurant.model.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Integer> {
    List<InventoryAdjustment> findByIngredient_IngredientIdOrderByAdjustedAtDesc(Integer ingredientId);
}
