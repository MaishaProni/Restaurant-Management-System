package com.restaurant.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.model.IngredientDto;
import com.restaurant.client.util.ApiClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InventoryService {

    private static final InventoryService instance = new InventoryService();

    private InventoryService() {}

    public static InventoryService getInstance() {
        return instance;
    }

    public CompletableFuture<List<IngredientDto>> getIngredients() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/inventory/ingredients", new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<IngredientDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load ingredients: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> addIngredient(Map<String, Object> body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().post("/inventory/ingredients", body,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add ingredient: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> adjustStock(int ingredientId, Map<String, Object> body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().post(
                        "/inventory/ingredients/" + ingredientId + "/adjust", body,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to adjust stock: " + ex.getMessage(), ex);
            }
        });
    }
}
