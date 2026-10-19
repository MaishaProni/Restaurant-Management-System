package com.restaurant.controller;

import com.restaurant.dto.AdjustmentRequest;
import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.IngredientRequest;
import com.restaurant.model.Ingredient;
import com.restaurant.model.InventoryAdjustment;
import com.restaurant.security.JwtUtil;
import com.restaurant.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final JwtUtil jwtUtil;

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<Ingredient>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllIngredients()));
    }

    @GetMapping("/ingredients/low-stock")
    public ResponseEntity<ApiResponse<List<Ingredient>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockIngredients()));
    }

    @PostMapping("/ingredients")
    public ResponseEntity<ApiResponse<Ingredient>> create(@Valid @RequestBody IngredientRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.createIngredient(req)));
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<ApiResponse<Ingredient>> update(
            @PathVariable Integer id, @Valid @RequestBody IngredientRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateIngredient(id, req)));
    }

    @PostMapping("/ingredients/{id}/adjust")
    public ResponseEntity<ApiResponse<Ingredient>> adjust(
            @PathVariable Integer id,
            @Valid @RequestBody AdjustmentRequest req,
            @RequestHeader("Authorization") String authHeader) {
        Integer managerId = extractUserId(authHeader);
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.adjustStock(id, managerId, req)));
    }

    @GetMapping("/ingredients/{id}/adjustments")
    public ResponseEntity<ApiResponse<List<InventoryAdjustment>>> getLog(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAdjustmentLog(id)));
    }

    private Integer extractUserId(String authHeader) {
        return jwtUtil.extractAllClaims(authHeader.replace("Bearer ", "")).get("userId", Integer.class);
    }
}
