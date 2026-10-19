package com.restaurant.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.MainApp;
import com.restaurant.client.model.*;
import com.restaurant.client.util.AlertUtil;
import com.restaurant.client.util.ApiClient;
import com.restaurant.client.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Map;

public class CashierBillingController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<OrderDto>  servedOrdersTable;
    @FXML private TableColumn<OrderDto, Integer> ordIdCol;
    @FXML private TableColumn<OrderDto, String>  ordTableCol;
    @FXML private TableColumn<OrderDto, String>  ordStatusCol;

    @FXML private TableView<OrderItemDto> billItemsTable;
    @FXML private TableColumn<OrderItemDto, String>  billItemNameCol;
    @FXML private TableColumn<OrderItemDto, Integer> billItemQtyCol;
    @FXML private TableColumn<OrderItemDto, Double>  billItemPriceCol;
    @FXML private TableColumn<OrderItemDto, Double>  billItemSubCol;

    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label serviceLabel;
    @FXML private Label grandTotalLabel;

    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton cardRadio;
    @FXML private RadioButton mobileRadio;
    @FXML private TextArea    receiptArea;

    private OrderDto selectedOrder;
    private BillDto  currentBill;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Cashier: " + SessionManager.getInstance().getFullName());
        setupTables();
        loadServedOrders();
    }

    private void setupTables() {
        ordIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        ordTableCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getTable() != null ?
                        "Table " + c.getValue().getTable().getTableNumber() : "?"));
        ordStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        billItemNameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getMenuItem() != null ? c.getValue().getMenuItem().getName() : ""));
        billItemQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        billItemPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        billItemSubCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getSubtotal()));

        servedOrdersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> { if (sel != null) loadBill(sel); });
    }

    // Runs HTTP on a background thread so the FX thread is never blocked.
    // After reload, the bill panel is cleared (user must re-select an order).
    // Does NOT touch the receiptArea so existing receipts stay readable.
    private void loadServedOrders() {
        com.restaurant.client.service.OrderService.getInstance().getOrders("SERVED")
            .thenAcceptAsync(orders -> {
                servedOrdersTable.setItems(FXCollections.observableArrayList(orders));
                clearBillInputs(); // clear inputs but leave receiptArea alone
            }, Platform::runLater)
            .exceptionally(e -> {
                Platform.runLater(() -> AlertUtil.error("Error", "Cannot load orders: " + e.getMessage()));
                return null;
            });
    }

    private void loadBill(OrderDto order) {
        selectedOrder = order;
        int orderId = order.getOrderId();
        List<OrderItemDto> items = order.getItems() != null ? order.getItems() : List.of();
        // Show items immediately; fetch totals in background so the selection
        // listener never blocks the FX thread
        billItemsTable.setItems(FXCollections.observableArrayList(items));
        receiptArea.clear();
        
        com.restaurant.client.service.BillingService.getInstance().getBill(orderId)
            .thenAcceptAsync(bill -> {
                currentBill = bill;
                subtotalLabel.setText(String.format("$%.2f", bill.getSubtotal()));
                taxLabel.setText(String.format("$%.2f", bill.getTaxAmount()));
                serviceLabel.setText(String.format("$%.2f", bill.getServiceCharge()));
                grandTotalLabel.setText(String.format("$%.2f", bill.getGrandTotal()));
            }, Platform::runLater)
            .exceptionally(e -> {
                Platform.runLater(() -> AlertUtil.error("Error", e.getMessage()));
                return null;
            });
    }

    @FXML private void handleProcessPayment() {
        if (selectedOrder == null || currentBill == null) {
            AlertUtil.warn("Select Order", "Please select a served order first."); return;
        }
        RadioButton sel = (RadioButton) paymentMethodGroup.getSelectedToggle();
        if (sel == null) { AlertUtil.warn("Payment Method", "Please select a payment method."); return; }
        String method = sel.getText().toUpperCase();
        if (!AlertUtil.confirm("Process Payment",
                String.format("Confirm %s payment of $%.2f?", method, currentBill.getGrandTotal()))) return;

        int orderId = selectedOrder.getOrderId();
        OrderDto paidOrder = selectedOrder;

        com.restaurant.client.service.BillingService.getInstance().processPayment(orderId, method)
            .thenAcceptAsync(receipt -> {
                // Show the receipt FIRST, then remove the order from the table.
                receiptArea.setText(receipt);
                servedOrdersTable.getItems().remove(paidOrder);
                servedOrdersTable.getSelectionModel().clearSelection();
                clearBillInputs(); // clear totals/items but keep receipt visible
                AlertUtil.info("Payment Successful", "Payment processed. Receipt generated.");
            }, Platform::runLater)
            .exceptionally(e -> {
                Platform.runLater(() -> AlertUtil.error("Payment Failed", e.getMessage()));
                return null;
            });
    }

    @FXML private void handleRefresh() {
        receiptArea.clear(); // user explicitly refreshed, it's OK to clear now
        loadServedOrders();
    }

    @FXML private void handleDownloadReceipt() {
        String receiptText = receiptArea.getText();
        if (receiptText == null || receiptText.trim().isEmpty()) {
            AlertUtil.warn("No Receipt", "There is no receipt to download.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("Receipt.txt");
        
        java.io.File file = fileChooser.showSaveDialog(receiptArea.getScene().getWindow());
        if (file != null) {
            try {
                java.nio.file.Files.writeString(file.toPath(), receiptText);
                AlertUtil.info("Success", "Receipt downloaded successfully.");
            } catch (java.io.IOException e) {
                AlertUtil.error("Error", "Failed to save receipt: " + e.getMessage());
            }
        }
    }

    // Clears bill inputs (totals, item list, selection state) but intentionally
    // leaves receiptArea untouched so the cashier can read/print the receipt.
    private void clearBillInputs() {
        billItemsTable.getItems().clear();
        subtotalLabel.setText("$0.00");
        taxLabel.setText("$0.00");
        serviceLabel.setText("$0.00");
        grandTotalLabel.setText("$0.00");
        selectedOrder = null;
        currentBill = null;
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clearSession();
        MainApp.showScene("Login");
    }
}
