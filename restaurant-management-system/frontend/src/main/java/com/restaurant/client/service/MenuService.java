package com.restaurant.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.model.MenuCategoryDto;
import com.restaurant.client.model.MenuItemDto;
import com.restaurant.client.model.RecipeItemDto;
import com.restaurant.client.util.ApiClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MenuService {

    private static final MenuService instance = new MenuService();

    private MenuService() {}

    public static MenuService getInstance() {
        return instance;
    }

    public CompletableFuture<List<MenuCategoryDto>> getCategories() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/menu/categories", new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<MenuCategoryDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load categories: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> addCategory(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().post("/menu/categories", Map.of("name", name),
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add category: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> deleteCategory(int categoryId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().delete("/menu/categories/" + categoryId,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to delete category: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<List<MenuItemDto>> getItems(boolean availableOnly) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String path = availableOnly ? "/menu/items?availableOnly=true" : "/menu/items";
                Map<String, Object> resp = ApiClient.getInstance().get(
                        path, new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<MenuItemDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load items: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> addItem(Map<String, Object> body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().post("/menu/items", body,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add item: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> updateItem(int itemId, Map<String, Object> body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().put("/menu/items/" + itemId, body,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to update item: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> deleteItem(int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().delete("/menu/items/" + itemId,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to delete item: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<List<RecipeItemDto>> getRecipeItems(int menuItemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/menu/items/" + menuItemId + "/recipe",
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<RecipeItemDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load recipe: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> addRecipeItem(int menuItemId, Map<String, Object> body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().post(
                        "/menu/items/" + menuItemId + "/recipe", body,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add recipe item: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> deleteRecipeItem(int menuItemId, int recipeItemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().delete(
                        "/menu/items/" + menuItemId + "/recipe/" + recipeItemId,
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to delete recipe item: " + ex.getMessage(), ex);
            }
        });
    }
}
