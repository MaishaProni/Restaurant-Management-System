package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.MenuCategoryRequest;
import com.restaurant.dto.MenuItemRequest;
import com.restaurant.dto.RecipeItemRequest;
import com.restaurant.model.MenuCategory;
import com.restaurant.model.MenuItem;
import com.restaurant.model.RecipeItem;
import com.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<MenuCategory>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAllCategories()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<MenuCategory>> createCategory(@Valid @RequestBody MenuCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.createCategory(req)));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<MenuCategory>> updateCategory(
            @PathVariable Integer id, @Valid @RequestBody MenuCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.updateCategory(id, req)));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        menuService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getItems(
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        List<MenuItem> items = availableOnly ? menuService.getAvailableItems() : menuService.getAllItems();
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<MenuItem>> createItem(@Valid @RequestBody MenuItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.createItem(req)));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<MenuItem>> updateItem(
            @PathVariable Integer id, @Valid @RequestBody MenuItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.updateItem(id, req)));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Integer id) {
        menuService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted", null));
    }

    // ── Recipe endpoints ──────────────────────────────────────────────────────

    @GetMapping("/items/{id}/recipe")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<RecipeItem>>> getRecipe(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getRecipeItems(id)));
    }

    @PostMapping("/items/{id}/recipe")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<RecipeItem>> addRecipeItem(
            @PathVariable Integer id, @RequestBody RecipeItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.addRecipeItem(id, req)));
    }

    @DeleteMapping("/items/{id}/recipe/{recipeItemId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteRecipeItem(
            @PathVariable Integer id, @PathVariable Integer recipeItemId) {
        menuService.deleteRecipeItem(recipeItemId);
        return ResponseEntity.ok(ApiResponse.ok("Ingredient removed from recipe", null));
    }
}
