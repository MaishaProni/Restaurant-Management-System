package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeItemDto {
    private Integer       recipeItemId;
    private IngredientDto ingredient;
    private Double        quantityRequired;

    public Integer       getRecipeItemId()      { return recipeItemId; }
    public IngredientDto getIngredient()         { return ingredient; }
    public Double        getQuantityRequired()   { return quantityRequired; }

    public void setRecipeItemId(Integer v)      { this.recipeItemId = v; }
    public void setIngredient(IngredientDto v)  { this.ingredient = v; }
    public void setQuantityRequired(Double v)   { this.quantityRequired = v; }
}
