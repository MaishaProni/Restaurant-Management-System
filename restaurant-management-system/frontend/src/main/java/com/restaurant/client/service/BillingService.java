package com.restaurant.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.model.BillDto;
import com.restaurant.client.util.ApiClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BillingService {

    private static final BillingService instance = new BillingService();

    private BillingService() {}

    public static BillingService getInstance() {
        return instance;
    }

    public CompletableFuture<BillDto> getBill(int orderId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/billing/orders/" + orderId + "/bill",
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                return ApiClient.getInstance().getMapper().readValue(json, BillDto.class);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot load bill: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<String> processPayment(int orderId, String method) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().post(
                        "/billing/orders/" + orderId + "/pay",
                        Map.of("method", method),
                        new TypeReference<Map<String, Object>>() {});
                return (String) resp.get("data"); // Returns the receipt string
            } catch (Exception ex) {
                throw new RuntimeException("Payment failed: " + ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<String> getReceipt(int orderId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/billing/orders/" + orderId + "/receipt",
                        new TypeReference<Map<String, Object>>() {});
                return (String) resp.get("data");
            } catch (Exception ex) {
                throw new RuntimeException("Could not fetch receipt: " + ex.getMessage(), ex);
            }
        });
    }
}
