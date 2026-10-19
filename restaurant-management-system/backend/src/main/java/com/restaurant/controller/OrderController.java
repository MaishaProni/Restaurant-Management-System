package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.OrderItemRequest;
import com.restaurant.dto.OrderRequest;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import com.restaurant.security.JwtUtil;
import com.restaurant.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getOrders(
            @RequestParam(required = false) String status) {
        List<Order> orders;
        if (status == null) {
            orders = orderService.getOrdersByStatus(OrderStatus.SERVED);
        } else if ("ALL".equalsIgnoreCase(status)) {
            orders = orderService.getAllOrders();
        } else {
            orders = orderService.getOrdersByStatus(OrderStatus.valueOf(status.toUpperCase()));
        }
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }

    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<ApiResponse<Order>> getActiveOrderForTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getActiveOrderForTable(tableId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @Valid @RequestBody OrderRequest req,
            @RequestHeader("Authorization") String authHeader) {
        Integer waiterId = extractUserId(authHeader);
        return ResponseEntity.ok(ApiResponse.ok(orderService.createOrder(req, waiterId)));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<Order>> addItem(
            @PathVariable Integer id,
            @Valid @RequestBody OrderItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.addItem(id, req)));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Order>> removeItem(
            @PathVariable Integer orderId, @PathVariable Integer itemId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.removeItem(orderId, itemId)));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Order>> confirmOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.confirmOrder(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(id)));
    }

    @PutMapping("/{id}/serve")
    public ResponseEntity<ApiResponse<Order>> markServed(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.markServed(id)));
    }

    private Integer extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractAllClaims(token).get("userId", Integer.class);
    }
}
