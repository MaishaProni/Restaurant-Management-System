package com.restaurant.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.model.LoginRequest;
import com.restaurant.client.util.ApiClient;
import com.restaurant.client.util.SessionManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthService {

    private static final AuthService instance = new AuthService();

    private AuthService() {}

    public static AuthService getInstance() {
        return instance;
    }

    public CompletableFuture<String> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> wrapper = ApiClient.getInstance().postNoAuth(
                        "/auth/login",
                        new LoginRequest(username, password),
                        new TypeReference<Map<String, Object>>() {}
                );

                if (Boolean.TRUE.equals(wrapper.get("success"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) wrapper.get("data");
                    String token    = (String)  data.get("token");
                    String fullName = (String)  data.get("fullName");
                    String role     = (String)  data.get("role");
                    Integer userId  = (Integer) data.get("userId");

                    SessionManager.getInstance().setSession(token, username, fullName, role, userId);
                    return role;
                } else {
                    throw new RuntimeException((String) wrapper.get("message"));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName(), ex);
            }
        });
    }

    public void logout() {
        SessionManager.getInstance().clearSession();
    }
}
