package com.restaurant.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.MainApp;
import com.restaurant.client.model.LoginRequest;
import com.restaurant.client.util.AlertUtil;
import com.restaurant.client.util.ApiClient;
import com.restaurant.client.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("");
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Logging in...");

        com.restaurant.client.service.AuthService.getInstance().login(username, password)
            .thenAcceptAsync(role -> {
                String scene = switch (role) {
                    case "MANAGER"  -> "ManagerDashboard";
                    case "WAITER"   -> "WaiterOrder";
                    case "KITCHEN"  -> "KitchenQueue";
                    case "CASHIER"  -> "CashierBilling";
                    default -> throw new RuntimeException("Unknown role: " + role);
                };
                MainApp.showScene(scene);
            }, javafx.application.Platform::runLater)
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText("Login failed: " + msg);
                    loginButton.setDisable(false);
                });
                return null;
            });
    }
}
