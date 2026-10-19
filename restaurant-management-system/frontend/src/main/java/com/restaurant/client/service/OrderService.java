package com.restaurant.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.model.OrderDto;
import com.restaurant.client.model.TableDto;
import com.restaurant.client.util.ApiClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OrderService {

    private static final OrderService instance = new OrderService();

    private OrderService() {}

    public static OrderService getInstance() {
        return instance;
    }

    public CompletableFuture<List<TableDto>> getTables() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/tables", new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<TableDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load tables: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<OrderDto> getActiveOrderForTable(int tableId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/orders/table/" + tableId + "/active",
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("No active order found for this table.", ex);
            }
        });
    }

    public CompletableFuture<OrderDto> getOrder(int orderId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/orders/" + orderId, new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load order: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<List<OrderDto>> getOrders(String status) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String param = (status == null || "ALL".equals(status)) ? "ALL" : status;
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/orders?status=" + param, new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<OrderDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load orders: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<OrderDto> createOrder(int tableId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().post(
                        "/orders", Map.of("tableId", tableId),
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create order: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<OrderDto> addItemToOrder(int orderId, Map<String, Object> body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().post(
                        "/orders/" + orderId + "/items", body,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add item: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<OrderDto> removeItemFromOrder(int orderId, int orderItemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().delete(
                        "/orders/" + orderId + "/items/" + orderItemId,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to remove item: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<OrderDto> updateOrderStatus(int orderId, String action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // action should be "confirm", "serve", "cancel"
                Map<String, Object> resp = ApiClient.getInstance().put(
                        "/orders/" + orderId + "/" + action, null,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to update order status: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<List<OrderDto>> getKitchenQueue() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/kitchen/queue", new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json,
                        new TypeReference<List<OrderDto>>() {});
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load kitchen queue: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Void> updateKitchenOrderStatus(int orderId, String status) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiClient.getInstance().put(
                        "/kitchen/orders/" + orderId + "/status",
                        Map.of("status", status),
                        new TypeReference<Map<String, Object>>() {});
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to update order status: " + ex.getMessage(), ex);
            }
        });
    }
}
