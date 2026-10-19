package com.restaurant.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restaurant.client.MainApp;
import com.restaurant.client.model.*;
import com.restaurant.client.util.AlertUtil;
import com.restaurant.client.util.ApiClient;
import com.restaurant.client.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

public class ManagerDashboardController {

    // --- Header ---
    @FXML private Label welcomeLabel;

    // --- Menu Tab ---
    @FXML private TableView<MenuCategoryDto>  categoryTable;
    @FXML private TableColumn<MenuCategoryDto, Integer> catIdCol;
    @FXML private TableColumn<MenuCategoryDto, String>  catNameCol;
    @FXML private TextField  catNameField;

    @FXML private TableView<MenuItemDto> itemTable;
    @FXML private TableColumn<MenuItemDto, Integer> itemIdCol;
    @FXML private TableColumn<MenuItemDto, String>  itemNameCol;
    @FXML private TableColumn<MenuItemDto, Double>  itemPriceCol;
    @FXML private TableColumn<MenuItemDto, Boolean> itemAvailCol;
    @FXML private TextField    itemNameField;
    @FXML private TextField    itemPriceField;
    @FXML private ComboBox<MenuCategoryDto> itemCategoryCombo;
    @FXML private CheckBox     itemAvailableCheck;

    // --- Recipe section (inside Menu Tab) ---
    @FXML private ComboBox<IngredientDto>        recipeIngredientCombo;
    @FXML private TextField                      recipeQtyField;
    @FXML private TableView<RecipeItemDto>       recipeTable;
    @FXML private TableColumn<RecipeItemDto, String> recipeIngNameCol;
    @FXML private TableColumn<RecipeItemDto, Double> recipeQtyCol;

    // --- Inventory Tab ---
    @FXML private TableView<IngredientDto>  ingredientTable;
    @FXML private TableColumn<IngredientDto, Integer> ingIdCol;
    @FXML private TableColumn<IngredientDto, String>  ingNameCol;
    @FXML private TableColumn<IngredientDto, Double>  ingStockCol;
    @FXML private TableColumn<IngredientDto, String>  ingUnitCol;
    @FXML private TableColumn<IngredientDto, Double>  ingThreshCol;
    @FXML private TextField ingNameField;
    @FXML private TextField ingStockField;
    @FXML private TextField ingUnitField;
    @FXML private TextField ingThreshField;
    @FXML private TextField adjustQtyField;
    @FXML private TextField adjustReasonField;
    @FXML private Label     lowStockLabel;

    // --- Order History Tab ---
    @FXML private ComboBox<String>   orderStatusFilter;
    @FXML private TableView<OrderDto> orderHistoryTable;
    @FXML private TableColumn<OrderDto, Integer> ohOrderIdCol;
    @FXML private TableColumn<OrderDto, String>  ohTableCol;
    @FXML private TableColumn<OrderDto, String>  ohWaiterCol;
    @FXML private TableColumn<OrderDto, String>  ohStatusCol;
    @FXML private TableColumn<OrderDto, String>  ohTotalCol;
    @FXML private TableColumn<OrderDto, String>  ohTimeCol;

    // --- Reports Tab ---
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label      salesSummaryLabel;
    @FXML private TableView<TopItemRow>  topItemTable;
    @FXML private TableColumn<TopItemRow, String>  topItemNameCol;
    @FXML private TableColumn<TopItemRow, Long>    topItemQtyCol;

    private List<MenuCategoryDto> categories    = new ArrayList<>();
    private List<IngredientDto>   allIngredients = new ArrayList<>();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getFullName());
        setupMenuTable();
        setupInventoryTable();
        setupOrderHistoryTable();
        setupReportsTable();
        loadCategories();
        loadItems();
        loadIngredients();
        loadOrderHistory();
    }

    // ========== MENU ==========

    private void setupMenuTable() {
        catIdCol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        catNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        itemIdCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        itemPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        itemAvailCol.setCellValueFactory(new PropertyValueFactory<>("isAvailable"));

        recipeIngNameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getIngredient() != null
                                ? c.getValue().getIngredient().getName() : ""));
        recipeQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityRequired"));

        // Load recipe items whenever the manager selects a menu item
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                loadRecipeItems(sel.getItemId());
            } else {
                recipeTable.setItems(FXCollections.observableArrayList());
            }
        });
    }

    private void loadCategories() {
        try {
            Map<String, Object> resp = ApiClient.getInstance().get(
                    "/menu/categories", new TypeReference<Map<String, Object>>() {});
            String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
            categories = ApiClient.getInstance().getMapper().readValue(json,
                    new TypeReference<List<MenuCategoryDto>>() {});
            categoryTable.setItems(FXCollections.observableArrayList(categories));
            itemCategoryCombo.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            AlertUtil.error("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadItems() {
        try {
            Map<String, Object> resp = ApiClient.getInstance().get(
                    "/menu/items", new TypeReference<Map<String, Object>>() {});
            String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
            List<MenuItemDto> items = ApiClient.getInstance().getMapper().readValue(json,
                    new TypeReference<List<MenuItemDto>>() {});
            itemTable.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            AlertUtil.error("Error", "Failed to load items: " + e.getMessage());
        }
    }

    @FXML private void handleAddCategory() {
        String name = catNameField.getText().trim();
        if (name.isEmpty()) { AlertUtil.warn("Input", "Category name required."); return; }
        try {
            ApiClient.getInstance().post("/menu/categories", Map.of("name", name),
                    new TypeReference<Map<String, Object>>() {});
            catNameField.clear();
            loadCategories();
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleDeleteCategory() {
        MenuCategoryDto sel = categoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select", "Please select a category first."); return; }
        if (!AlertUtil.confirm("Delete", "Delete category '" + sel.getName() + "'?")) return;
        try {
            ApiClient.getInstance().delete("/menu/categories/" + sel.getCategoryId(),
                    new TypeReference<Map<String, Object>>() {});
            loadCategories();
            loadItems();
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleAddItem() {
        String name = itemNameField.getText().trim();
        String priceStr = itemPriceField.getText().trim();
        MenuCategoryDto cat = itemCategoryCombo.getValue();
        if (name.isEmpty() || priceStr.isEmpty() || cat == null) {
            AlertUtil.warn("Input", "Name, price, and category are required."); return;
        }
        try {
            double price = Double.parseDouble(priceStr);
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("price", price);
            body.put("categoryId", cat.getCategoryId());
            body.put("isAvailable", itemAvailableCheck.isSelected());
            ApiClient.getInstance().post("/menu/items", body,
                    new TypeReference<Map<String, Object>>() {});
            itemNameField.clear(); itemPriceField.clear();
            loadItems();
        } catch (NumberFormatException ex) {
            AlertUtil.error("Error", "Price must be a valid number.");
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleToggleItemAvailability() {
        MenuItemDto sel = itemTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select", "Please select a menu item."); return; }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", sel.getName());
            body.put("price", sel.getPrice());
            body.put("categoryId", sel.getCategory() != null ? sel.getCategory().getCategoryId() : null);
            body.put("isAvailable", !Boolean.TRUE.equals(sel.getIsAvailable()));
            ApiClient.getInstance().put("/menu/items/" + sel.getItemId(), body,
                    new TypeReference<Map<String, Object>>() {});
            loadItems();
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleDeleteItem() {
        MenuItemDto sel = itemTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select", "Please select a menu item."); return; }
        if (!AlertUtil.confirm("Delete", "Delete '" + sel.getName() + "'?")) return;
        try {
            ApiClient.getInstance().delete("/menu/items/" + sel.getItemId(),
                    new TypeReference<Map<String, Object>>() {});
            recipeTable.setItems(FXCollections.observableArrayList());
            loadItems();
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    // ── Recipe handlers ───────────────────────────────────────────────────────

    private void loadRecipeItems(int menuItemId) {
        try {
            Map<String, Object> resp = ApiClient.getInstance().get(
                    "/menu/items/" + menuItemId + "/recipe",
                    new TypeReference<Map<String, Object>>() {});
            String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
            List<RecipeItemDto> items = ApiClient.getInstance().getMapper().readValue(json,
                    new TypeReference<List<RecipeItemDto>>() {});
            recipeTable.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            AlertUtil.error("Error", "Failed to load recipe: " + e.getMessage());
        }
    }

    @FXML private void handleAddRecipeItem() {
        MenuItemDto selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) { AlertUtil.warn("Select Item", "Select a menu item first."); return; }
        IngredientDto ing = recipeIngredientCombo.getValue();
        if (ing == null) { AlertUtil.warn("Select Ingredient", "Select an ingredient."); return; }
        String qtyStr = recipeQtyField.getText().trim();
        if (qtyStr.isEmpty()) { AlertUtil.warn("Input", "Quantity required is required."); return; }
        try {
            double qty = Double.parseDouble(qtyStr);
            Map<String, Object> body = new HashMap<>();
            body.put("ingredientId", ing.getIngredientId());
            body.put("quantityRequired", qty);
            ApiClient.getInstance().post(
                    "/menu/items/" + selectedItem.getItemId() + "/recipe", body,
                    new TypeReference<Map<String, Object>>() {});
            recipeQtyField.clear();
            loadRecipeItems(selectedItem.getItemId());
        } catch (NumberFormatException e) {
            AlertUtil.error("Error", "Quantity must be a valid number.");
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleDeleteRecipeItem() {
        MenuItemDto selectedItem = itemTable.getSelectionModel().getSelectedItem();
        RecipeItemDto selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedRecipe == null) {
            AlertUtil.warn("Select", "Select a menu item and an ingredient from its recipe."); return;
        }
        String ingName = selectedRecipe.getIngredient() != null ? selectedRecipe.getIngredient().getName() : "ingredient";
        if (!AlertUtil.confirm("Remove", "Remove " + ingName + " from " + selectedItem.getName() + "'s recipe?")) return;
        try {
            ApiClient.getInstance().delete(
                    "/menu/items/" + selectedItem.getItemId() + "/recipe/" + selectedRecipe.getRecipeItemId(),
                    new TypeReference<Map<String, Object>>() {});
            loadRecipeItems(selectedItem.getItemId());
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    // ========== INVENTORY ==========

    private void setupInventoryTable() {
        ingIdCol.setCellValueFactory(new PropertyValueFactory<>("ingredientId"));
        ingNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ingStockCol.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        ingUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        ingThreshCol.setCellValueFactory(new PropertyValueFactory<>("lowStockThreshold"));
    }

    private void loadIngredients() {
        try {
            Map<String, Object> resp = ApiClient.getInstance().get(
                    "/inventory/ingredients", new TypeReference<Map<String, Object>>() {});
            String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
            List<IngredientDto> ings = ApiClient.getInstance().getMapper().readValue(json,
                    new TypeReference<List<IngredientDto>>() {});
            ingredientTable.setItems(FXCollections.observableArrayList(ings));
            allIngredients = ings;
            recipeIngredientCombo.setItems(FXCollections.observableArrayList(ings));

            long lowCount = ings.stream().filter(IngredientDto::isLowStock).count();
            lowStockLabel.setText(lowCount > 0 ? "⚠ " + lowCount + " ingredient(s) low on stock!" : "All stock levels OK.");
        } catch (Exception e) {
            AlertUtil.error("Error", "Failed to load ingredients: " + e.getMessage());
        }
    }

    @FXML private void handleAddIngredient() {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", ingNameField.getText().trim());
            body.put("currentStock", Double.parseDouble(ingStockField.getText().trim()));
            body.put("unit", ingUnitField.getText().trim());
            body.put("lowStockThreshold", ingThreshField.getText().isEmpty() ? 0.0 :
                    Double.parseDouble(ingThreshField.getText().trim()));
            ApiClient.getInstance().post("/inventory/ingredients", body,
                    new TypeReference<Map<String, Object>>() {});
            ingNameField.clear(); ingStockField.clear(); ingUnitField.clear(); ingThreshField.clear();
            loadIngredients();
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleAdjustStock() {
        IngredientDto sel = ingredientTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.warn("Select", "Please select an ingredient."); return; }
        String qtyStr = adjustQtyField.getText().trim();
        String reason = adjustReasonField.getText().trim();
        if (qtyStr.isEmpty() || reason.isEmpty()) {
            AlertUtil.warn("Input", "Quantity and reason are required."); return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("quantityChanged", Double.parseDouble(qtyStr));
            body.put("reason", reason);
            ApiClient.getInstance().post(
                    "/inventory/ingredients/" + sel.getIngredientId() + "/adjust", body,
                    new TypeReference<Map<String, Object>>() {});
            adjustQtyField.clear(); adjustReasonField.clear();
            loadIngredients();
            AlertUtil.info("Success", "Stock adjusted for " + sel.getName());
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    // ========== ORDER HISTORY ==========

    private void setupOrderHistoryTable() {
        orderStatusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "CONFIRMED", "PREPARING", "READY", "SERVED", "PAID", "CANCELLED"));
        orderStatusFilter.setValue("ALL");

        ohOrderIdCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOrderId()));
        ohTableCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getTable() != null
                                ? "Table " + c.getValue().getTable().getTableNumber() : "?"));
        ohWaiterCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getWaiter() != null
                                ? c.getValue().getWaiter().getFullName() : "?"));
        ohStatusCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        ohTotalCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("$%.2f", c.getValue().getTotal())));
        ohTimeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCreatedAt()));
    }

    @FXML private void handleLoadOrderHistory() {
        loadOrderHistory();
    }

    // ========== REPORTS ==========

    public static class TopItemRow {
        private final String itemName;
        private final Long   quantitySold;
        TopItemRow(String n, Long q) { this.itemName = n; this.quantitySold = q; }
        public String getItemName()    { return itemName; }
        public Long   getQuantitySold(){ return quantitySold; }
    }

    private void setupReportsTable() {
        topItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        topItemQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today);
    }

    private void loadOrderHistory() {
        String filter = orderStatusFilter.getValue();
        String param  = (filter == null || "ALL".equals(filter)) ? "ALL" : filter;
        try {
            Map<String, Object> resp = ApiClient.getInstance().get(
                    "/orders?status=" + param, new TypeReference<Map<String, Object>>() {});
            String json = ApiClient.getInstance().getMapper().writeValueAsString(resp.get("data"));
            List<OrderDto> orders = ApiClient.getInstance().getMapper().readValue(json,
                    new TypeReference<List<OrderDto>>() {});
            orderHistoryTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            AlertUtil.error("Error", "Failed to load orders: " + e.getMessage());
        }
    }

    @FXML private void handleGenerateReport() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            AlertUtil.warn("Input", "Please select start and end dates."); return;
        }
        String start = startDatePicker.getValue().toString();
        String end   = endDatePicker.getValue().toString();
        try {
            Map<String, Object> salesResp = ApiClient.getInstance().get(
                    "/reports/sales?start=" + start + "&end=" + end,
                    new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) salesResp.get("data");
            salesSummaryLabel.setText(String.format("Orders: %s | Revenue: $%.2f",
                    data.get("totalOrders"), ((Number) data.get("totalRevenue")).doubleValue()));

            Map<String, Object> topResp = ApiClient.getInstance().get(
                    "/reports/top-items?start=" + start + "&end=" + end,
                    new TypeReference<Map<String, Object>>() {});
            String topJson = ApiClient.getInstance().getMapper().writeValueAsString(topResp.get("data"));
            List<Map<String, Object>> topList = ApiClient.getInstance().getMapper().readValue(topJson,
                    new TypeReference<List<Map<String, Object>>>() {});
            List<TopItemRow> rows = new ArrayList<>();
            for (Map<String, Object> item : topList) {
                rows.add(new TopItemRow((String) item.get("itemName"),
                        ((Number) item.get("quantitySold")).longValue()));
            }
            topItemTable.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception e) {
            AlertUtil.error("Error", e.getMessage());
        }
    }

    @FXML private void handleExportCsv() {
        if (topItemTable.getItems().isEmpty()) {
            AlertUtil.warn("No Data", "Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("Sales_Report.csv");

        File file = fileChooser.showSaveDialog(topItemTable.getScene().getWindow());
        if (file != null) {
            try {
                StringBuilder csv = new StringBuilder();
                csv.append("Sales Summary\n");
                csv.append(salesSummaryLabel.getText()).append("\n\n");
                csv.append("Top-Selling Items\n");
                csv.append("Item Name,Quantity Sold\n");

                for (TopItemRow row : topItemTable.getItems()) {
                    csv.append("\"").append(row.getItemName()).append("\",")
                       .append(row.getQuantitySold()).append("\n");
                }

                Files.writeString(file.toPath(), csv.toString());
                AlertUtil.info("Success", "Report exported to CSV successfully.");
            } catch (Exception e) {
                AlertUtil.error("Export Failed", "Could not write to file: " + e.getMessage());
            }
        }
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clearSession();
        MainApp.showScene("Login");
    }
}
