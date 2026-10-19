package com.restaurant.dto;

public class RecipeItemRequest {
    private Integer ingredientId;
    private Double  quantityRequired;

    public Integer getIngredientId()       { return ingredientId; }
    public Double  getQuantityRequired()   { return quantityRequired; }
    public void setIngredientId(Integer v)     { this.ingredientId = v; }
    public void setQuantityRequired(Double v)  { this.quantityRequired = v; }
}
