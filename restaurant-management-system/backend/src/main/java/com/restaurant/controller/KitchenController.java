package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.model.Order;
import com.restaurant.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<Order>>> getQueue() {
        return ResponseEntity.ok(ApiResponse.ok(kitchenService.getActiveQueue()));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(ApiResponse.ok(kitchenService.updateStatus(id, status)));
    }
}
