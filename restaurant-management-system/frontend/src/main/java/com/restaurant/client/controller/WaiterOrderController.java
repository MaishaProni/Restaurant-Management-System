package com.restaurant.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.MainApp;
import com.restaurant.client.model.*;
import com.restaurant.client.util.AlertUtil;
import com.restaurant.client.util.ApiClient;
import com.restaurant.client.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.*;

public class WaiterOrderController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<TableDto> tableListView;
    @FXML private ComboBox<MenuCategoryDto> categoryCombo;
    @FXML private TableView<MenuItemDto> menuTable;
    @FXML private TableColumn<MenuItemDto, String>  menuNameCol;
    @FXML private TableColumn<MenuItemDto, Double>  menuPriceCol;
    @FXML private TableView<OrderItemDto> orderTable;
    @FXML private TableColumn<OrderItemDto, String>  orderItemNameCol;
    @FXML private TableColumn<OrderItemDto, Integer> orderItemQtyCol;
    @FXML private TableColumn<OrderItemDto, Double>  orderItemPriceCol;
    @FXML private Label  orderTotalLabel;
    @FXML private Label  currentOrderLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TextField notesField;
    @FXML private Button addItemBtn;
    @FXML private Button confirmOrderBtn;
    @FXML private Button markServedBtn;
    @FXML private Button cancelOrderBtn;
    @FXML private Button removeItemBtn;

    private OrderDto currentOrder;
    private final ObservableList<MenuItemDto> allMenuItems = FXCollections.observableArrayList();
    private Timeline orderRefreshTimeline;
    private volatile int autoLoadGeneration = 0;
    // Tracks which table we're currently working on, independently of currentOrder.
    // Used to detect when loadTables() programmatically re-selects the same table
    // (because ListView preserves the selected index on setItems()), so we can skip
    // the spurious autoLoadOrderForTable() call that would race with handleAddItem().
    private Integer currentTableId = null;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Waiter: " + SessionManager.getInstance().getFullName());
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));

        setupColumns();
        loadTables();
        loadMenuData();
        updateOrderPanel();

        tableListView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null && "OCCUPIED".equals(sel.getStatus())) {
                // JavaFX ListView preserves the selected INDEX when setItems() is called with a new
                // list. That means loadTables() re-fires this listener for the same table (now
                // OCCUPIED) even though we already have its order loaded. Guard against that by
                // tracking currentTableId independently — if the ID hasn't changed, skip the reload.
                if (!Objects.equals(currentTableId, sel.getTableId())) {
                    currentTableId = sel.getTableId();
                    currentOrder = null;
                    updateOrderPanel();
                    autoLoadOrderForTable(sel.getTableId());
                }
            } else if (sel != null) {
                currentTableId = null;
                currentOrder = null;
                updateOrderPanel();
            }
            // sel == null: programmatic clear from loadTables() — preserve current state
        });

        orderTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> updateRemoveButtonState());

        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null && !addItemBtn.isDisabled()) {
                addItemBtn.requestFocus();
            }
        });
    }

    private void setupColumns() {
        menuNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        menuPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        orderItemNameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getMenuItem() != null ? c.getValue().getMenuItem().getName() : ""));
        orderItemQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderItemPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
    }

    private void updateRemoveButtonState() {
        boolean canRemove = currentOrder != null
                && "PENDING".equals(currentOrder.getStatus())
                && orderTable.getSelectionModel().getSelectedItem() != null;
        removeItemBtn.setDisable(!canRemove);
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadTables() {
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/tables", new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                List<TableDto> tables = ApiClient.getInstance().getMapper().readValue(
                        json, new TypeReference<List<TableDto>>() {});
                Platform.runLater(() ->
                        tableListView.setItems(FXCollections.observableArrayList(tables)));
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtil.error("Error", "Cannot load tables: " + e.getMessage()));
            }
        }).start();
    }

    private void loadMenuData() {
        try {
            Map<String, Object> catResp = ApiClient.getInstance().get(
                    "/menu/categories", new TypeReference<Map<String, Object>>() {});
            String catJson = ApiClient.getInstance().getMapper().writeValueAsString(catResp.get("data"));
            List<MenuCategoryDto> cats = ApiClient.getInstance().getMapper().readValue(
                    catJson, new TypeReference<List<MenuCategoryDto>>() {});
            MenuCategoryDto all = new MenuCategoryDto();
            all.setCategoryId(-1); all.setName("All Categories");
            List<MenuCategoryDto> withAll = new ArrayList<>();
            withAll.add(all); withAll.addAll(cats);
            categoryCombo.setItems(FXCollections.observableArrayList(withAll));
            categoryCombo.setValue(all);

            Map<String, Object> itemResp = ApiClient.getInstance().get(
                    "/menu/items?availableOnly=true", new TypeReference<Map<String, Object>>() {});
            String itemJson = ApiClient.getInstance().getMapper().writeValueAsString(itemResp.get("data"));
            List<MenuItemDto> items = ApiClient.getInstance().getMapper().readValue(
                    itemJson, new TypeReference<List<MenuItemDto>>() {});
            allMenuItems.setAll(items);
            menuTable.setItems(allMenuItems);
        } catch (Exception e) {
            AlertUtil.error("Error", "Cannot load menu: " + e.getMessage());
        }
    }

    private void autoLoadOrderForTable(int tableId) {
        final int gen = ++autoLoadGeneration;
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/orders/table/" + tableId + "/active",
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto order = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    // Discard if a newer load or an addItem call has since fired
                    if (gen == autoLoadGeneration) {
                        currentOrder = order;
                        updateOrderPanel();
                    }
                });
            } catch (Exception ignored) {
                // AVAILABLE table with no order, or order already closed — nothing to load
            }
        }).start();
    }

    // ─── Button handlers ──────────────────────────────────────────────────────

    @FXML private void handleCategoryFilter() {
        MenuCategoryDto sel = categoryCombo.getValue();
        if (sel == null || sel.getCategoryId() == -1) {
            menuTable.setItems(allMenuItems);
        } else {
            ObservableList<MenuItemDto> filtered = FXCollections.observableArrayList();
            for (MenuItemDto item : allMenuItems) {
                if (item.getCategory() != null &&
                        Objects.equals(item.getCategory().getCategoryId(), sel.getCategoryId())) {
                    filtered.add(item);
                }
            }
            menuTable.setItems(filtered);
        }
    }

    @FXML private void handleCreateOrder() {
        TableDto selectedTable = tableListView.getSelectionModel().getSelectedItem();
        if (selectedTable == null) {
            AlertUtil.warn("Select Table", "Please select a table."); return;
        }
        if (!"AVAILABLE".equals(selectedTable.getStatus())) {
            AlertUtil.warn("Table Occupied",
                    "This table already has an order. Use 'Load Table's Order' to resume it."); return;
        }
        int tableId     = selectedTable.getTableId();
        int tableNumber = selectedTable.getTableNumber();
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().post(
                        "/orders", Map.of("tableId", tableId),
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto order = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentTableId = tableId;
                    currentOrder = order;
                    loadTables();
                    updateOrderPanel();
                    AlertUtil.info("Order Created",
                            "Order #" + order.getOrderId() + " started for Table " + tableNumber);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Error", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleLoadTableOrder() {
        TableDto selectedTable = tableListView.getSelectionModel().getSelectedItem();
        if (selectedTable == null) {
            AlertUtil.warn("Select Table", "Please select a table first."); return;
        }
        int tableId = selectedTable.getTableId();
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/orders/table/" + tableId + "/active",
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto order = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentTableId = tableId;
                    currentOrder = order;
                    updateOrderPanel();
                    AlertUtil.info("Order Loaded",
                            "Loaded Order #" + order.getOrderId() + " [" + order.getStatus() + "]");
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtil.error("No Active Order", "No active order found for this table."));
            }
        }).start();
    }

    @FXML private void handleAddItem() {
        if (currentOrder == null) { AlertUtil.warn("No Order", "Create an order first."); return; }
        if (!"PENDING".equals(currentOrder.getStatus())) {
            AlertUtil.warn("Cannot Add", "Items can only be added to PENDING orders."); return;
        }
        MenuItemDto sel = menuTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select Item", "Please select a menu item."); return; }

        // Capture all UI values on the FX thread BEFORE going to background.
        // Increment generation to cancel any in-flight autoLoadOrderForTable callback
        // that would otherwise overwrite the result we're about to get back.
        autoLoadGeneration++;
        int    orderId    = currentOrder.getOrderId();
        int    menuItemId = sel.getItemId();
        int    qty        = quantitySpinner.getValue();
        String notes      = notesField.getText().trim();
        notesField.clear();
        addItemBtn.setDisable(true);

        Map<String, Object> body = new HashMap<>();
        body.put("menuItemId", menuItemId);
        body.put("quantity",   qty);
        body.put("notes",      notes.isEmpty() ? null : notes);

        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().post(
                        "/orders/" + orderId + "/items", body,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto updated = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentOrder = updated;
                    updateOrderPanel(); // re-enables addItemBtn for PENDING orders
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addItemBtn.setDisable(false); // restore on failure
                    AlertUtil.error("Cannot Add Item", e.getMessage());
                });
            }
        }).start();
    }

    @FXML private void handleRemoveItem() {
        if (currentOrder == null) { AlertUtil.warn("No Order", "No active order."); return; }
        if (!"PENDING".equals(currentOrder.getStatus())) {
            AlertUtil.warn("Cannot Remove", "Items can only be removed from PENDING orders."); return;
        }
        OrderItemDto sel = orderTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.warn("Select Item", "Please select an item from the order list."); return;
        }

        int orderId     = currentOrder.getOrderId();
        int orderItemId = sel.getOrderItemId();
        removeItemBtn.setDisable(true); // prevent double-fire

        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().delete(
                        "/orders/" + orderId + "/items/" + orderItemId,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto updated = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentOrder = updated;
                    updateOrderPanel();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateRemoveButtonState(); // restore correct state on failure
                    AlertUtil.error("Error", e.getMessage());
                });
            }
        }).start();
    }

    @FXML private void handleConfirmOrder() {
        if (currentOrder == null) return;
        if (!AlertUtil.confirm("Confirm Order",
                "Send order #" + currentOrder.getOrderId() + " to kitchen?")) return;
        int orderId = currentOrder.getOrderId();
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().put(
                        "/orders/" + orderId + "/confirm", null,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto updated = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentOrder = updated;
                    updateOrderPanel();
                    AlertUtil.info("Sent to Kitchen", "Order confirmed and sent to kitchen.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Error", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleMarkServed() {
        if (currentOrder == null) return;
        int orderId = currentOrder.getOrderId();
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().put(
                        "/orders/" + orderId + "/serve", null,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto updated = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentOrder = updated;
                    updateOrderPanel();
                    loadTables();
                    AlertUtil.info("Marked Served", "Order marked as SERVED. Ready for cashier.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Error", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleCancelOrder() {
        if (currentOrder == null) return;
        if (!AlertUtil.confirm("Cancel Order",
                "Are you sure you want to cancel order #" + currentOrder.getOrderId() + "?")) return;
        int orderId = currentOrder.getOrderId();
        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().put(
                        "/orders/" + orderId + "/cancel", null,
                        new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto updated = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                Platform.runLater(() -> {
                    currentOrder = null; // Clear it so they don't see a cancelled order as active
                    currentTableId = null;
                    updateOrderPanel();
                    loadTables();
                    AlertUtil.info("Order Cancelled", "Order #" + orderId + " has been cancelled.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Error", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleNewOrder() {
        currentTableId = null;
        stopOrderRefresh();
        currentOrder = null;
        updateOrderPanel();
        loadTables();
    }

    // ─── Order status polling ─────────────────────────────────────────────────

    private void startOrderRefresh() {
        if (orderRefreshTimeline != null) orderRefreshTimeline.stop();
        orderRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> refreshCurrentOrder()));
        orderRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        orderRefreshTimeline.play();
    }

    private void stopOrderRefresh() {
        if (orderRefreshTimeline != null) {
            orderRefreshTimeline.stop();
            orderRefreshTimeline = null;
        }
    }

    // Timeline fires on the FX thread, so move the HTTP call to a background
    // thread. Capture orderId/oldStatus before leaving the FX thread.
    private void refreshCurrentOrder() {
        if (currentOrder == null) { stopOrderRefresh(); return; }
        Integer orderId   = currentOrder.getOrderId();
        String  oldStatus = currentOrder.getStatus();

        new Thread(() -> {
            try {
                Map<String, Object> resp = ApiClient.getInstance().get(
                        "/orders/" + orderId, new TypeReference<Map<String, Object>>() {});
                String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
                OrderDto refreshed = ApiClient.getInstance().getMapper().readValue(json, OrderDto.class);
                String newStatus = refreshed.getStatus();

                if (!newStatus.equals(oldStatus)) {
                    Platform.runLater(() -> {
                        if ("PAID".equals(newStatus)) {
                            stopOrderRefresh();
                            currentTableId = null;
                            currentOrder = null;
                            updateOrderPanel();
                            loadTables();
                            AlertUtil.info("Table Available",
                                    "Order #" + orderId + " has been paid. Table is now available.");
                        } else {
                            currentOrder = refreshed;
                            updateOrderPanel();
                            if ("READY".equals(newStatus)) {
                                AlertUtil.info("Order Ready!",
                                        "Order #" + orderId + " is READY — please serve the table.");
                            }
                        }
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    // ─── Panel state ──────────────────────────────────────────────────────────

    private void updateOrderPanel() {
        if (currentOrder == null) {
            currentOrderLabel.setText("No active order");
            orderTable.setItems(FXCollections.observableArrayList());
            orderTotalLabel.setText("Total: $0.00");
            addItemBtn.setDisable(true);
            confirmOrderBtn.setDisable(true);
            markServedBtn.setDisable(true);
            cancelOrderBtn.setDisable(true);
            removeItemBtn.setDisable(true);
            stopOrderRefresh();
            return;
        }

        currentOrderLabel.setText("Order #" + currentOrder.getOrderId()
                + " — Table " + (currentOrder.getTable() != null
                        ? currentOrder.getTable().getTableNumber() : "?")
                + " [" + currentOrder.getStatus() + "]");

        // Preserve the currently-selected row across the table refresh.
        // Without this, setItems() always clears the selection, which means
        // clicking Add and then immediately clicking Remove would fail because
        // the Remove selection was wiped by the Add's updateOrderPanel() call.
        Integer prevSelectedId = null;
        if (orderTable.getSelectionModel().getSelectedItem() != null) {
            prevSelectedId = orderTable.getSelectionModel().getSelectedItem().getOrderItemId();
        }

        List<OrderItemDto> items = currentOrder.getItems() != null
                ? currentOrder.getItems() : List.of();
        orderTable.setItems(FXCollections.observableArrayList(items));

        // Restore selection to the same item by orderItemId if it still exists
        if (prevSelectedId != null) {
            final Integer targetId = prevSelectedId;
            for (int i = 0; i < orderTable.getItems().size(); i++) {
                if (Objects.equals(orderTable.getItems().get(i).getOrderItemId(), targetId)) {
                    orderTable.getSelectionModel().select(i);
                    break;
                }
            }
        }

        orderTotalLabel.setText(String.format("Total: $%.2f", currentOrder.getTotal()));

        boolean isPending = "PENDING".equals(currentOrder.getStatus());
        boolean isReady   = "READY".equals(currentOrder.getStatus());
        boolean isServed  = "SERVED".equals(currentOrder.getStatus());
        boolean inKitchen = "CONFIRMED".equals(currentOrder.getStatus())
                         || "PREPARING".equals(currentOrder.getStatus());
        boolean canCancel = isPending || "CONFIRMED".equals(currentOrder.getStatus());

        addItemBtn.setDisable(!isPending);
        confirmOrderBtn.setDisable(!isPending);
        markServedBtn.setDisable(!isReady);
        cancelOrderBtn.setDisable(!canCancel);
        updateRemoveButtonState();

        // Poll while kitchen works AND while awaiting cashier payment
        if (inKitchen || isServed) {
            if (orderRefreshTimeline == null) startOrderRefresh();
        } else {
            stopOrderRefresh();
        }
    }

    @FXML private void handleLogout() {
        stopOrderRefresh();
        SessionManager.getInstance().clearSession();
        MainApp.showScene("Login");
    }
}
