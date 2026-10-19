package com.restaurant.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.MainApp;
import com.restaurant.client.model.OrderDto;
import com.restaurant.client.model.OrderItemDto;
import com.restaurant.client.util.AlertUtil;
import com.restaurant.client.util.ApiClient;
import com.restaurant.client.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class KitchenQueueController {

    @FXML private Label welcomeLabel;
    @FXML private Label autoRefreshLabel;
    @FXML private TableView<OrderDto> queueTable;
    @FXML private TableColumn<OrderDto, Integer> orderIdCol;
    @FXML private TableColumn<OrderDto, String>  tableCol;
    @FXML private TableColumn<OrderDto, String>  statusCol;
    @FXML private TableColumn<OrderDto, String>  timeCol;
    @FXML private TextArea itemsDetailArea;

    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Kitchen: " + SessionManager.getInstance().getFullName());
        setupTable();
        loadQueue();
        startAutoRefresh();
    }

    private void setupTable() {
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        tableCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getTable() != null ?
                        "Table " + c.getValue().getTable().getTableNumber() : "?"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) showOrderItems(sel);
        });
    }

    private void loadQueue() {
        com.restaurant.client.service.OrderService.getInstance().getKitchenQueue()
            .thenAcceptAsync(orders -> {
                queueTable.setItems(FXCollections.observableArrayList(orders));
                autoRefreshLabel.setText("Queue updated. " + orders.size() + " active order(s).");
            }, Platform::runLater)
            .exceptionally(e -> {
                Platform.runLater(() -> autoRefreshLabel.setText("Refresh failed: " + e.getMessage()));
                return null;
            });
    }

    private void showOrderItems(OrderDto order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(order.getOrderId())
          .append(" | Table ").append(order.getTable() != null ? order.getTable().getTableNumber() : "?")
          .append(" | Status: ").append(order.getStatus()).append("\n");
        sb.append("─".repeat(40)).append("\n");
        if (order.getItems() != null) {
            for (OrderItemDto item : order.getItems()) {
                sb.append("  x").append(item.getQuantity()).append("  ");
                sb.append(item.getMenuItem() != null ? item.getMenuItem().getName() : "?");
                if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                    sb.append("  [Note: ").append(item.getNotes()).append("]");
                }
                sb.append("\n");
                
                if (item.getMenuItem() != null && item.getMenuItem().getRecipeItems() != null && !item.getMenuItem().getRecipeItems().isEmpty()) {
                    sb.append("    Recipe:\n");
                    for (com.restaurant.client.model.RecipeItemDto recipeItem : item.getMenuItem().getRecipeItems()) {
                        sb.append("      - ").append(recipeItem.getIngredient() != null ? recipeItem.getIngredient().getName() : "Unknown")
                          .append(": ").append(recipeItem.getQuantityRequired())
                          .append(" ").append(recipeItem.getIngredient() != null ? recipeItem.getIngredient().getUnit() : "")
                          .append("\n");
                    }
                }
            }
        }
        itemsDetailArea.setText(sb.toString());
    }

    @FXML private void handleMarkPreparing() {
        OrderDto sel = queueTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select", "Please select an order."); return; }
        updateStatus(sel.getOrderId(), "PREPARING");
    }

    @FXML private void handleMarkReady() {
        OrderDto sel = queueTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select", "Please select an order."); return; }
        updateStatus(sel.getOrderId(), "READY");
    }

    private void updateStatus(Integer orderId, String status) {
        com.restaurant.client.service.OrderService.getInstance().updateKitchenOrderStatus(orderId, status)
            .thenRunAsync(() -> {
                itemsDetailArea.clear();
                loadQueue();
            }, Platform::runLater)
            .exceptionally(e -> {
                Platform.runLater(() -> AlertUtil.error("Error", e.getMessage()));
                return null;
            });
    }

    @FXML private void handleRefresh() {
        loadQueue();
    }

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadQueue()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    @FXML private void handleLogout() {
        if (refreshTimeline != null) refreshTimeline.stop();
        SessionManager.getInstance().clearSession();
        MainApp.showScene("Login");
    }
}
