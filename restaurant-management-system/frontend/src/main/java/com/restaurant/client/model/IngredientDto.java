package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngredientDto {
    private Integer ingredientId;
    private String  name;
    private Double  currentStock;
    private String  unit;
    private Double  lowStockThreshold;

    public Integer getIngredientId()      { return ingredientId; }
    public String  getName()              { return name; }
    public Double  getCurrentStock()      { return currentStock; }
    public String  getUnit()              { return unit; }
    public Double  getLowStockThreshold() { return lowStockThreshold; }

    public void setIngredientId(Integer ingredientId)        { this.ingredientId = ingredientId; }
    public void setName(String name)                         { this.name = name; }
    public void setCurrentStock(Double currentStock)         { this.currentStock = currentStock; }
    public void setUnit(String unit)                         { this.unit = unit; }
    public void setLowStockThreshold(Double lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public boolean isLowStock() {
        return currentStock != null && lowStockThreshold != null && currentStock <= lowStockThreshold;
    }

    @Override
    public String toString() { return name; }
}
