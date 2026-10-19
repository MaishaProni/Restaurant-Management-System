package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuItemDto {
    private Integer itemId;
    private String name;
    private Double price;
    private MenuCategoryDto category;
    private Boolean isAvailable;
    private List<RecipeItemDto> recipeItems;

    public Integer        getItemId()     { return itemId; }
    public String         getName()       { return name; }
    public Double         getPrice()      { return price; }
    public MenuCategoryDto getCategory()  { return category; }
    public Boolean        getIsAvailable(){ return isAvailable; }
    public List<RecipeItemDto> getRecipeItems() { return recipeItems; }

    public void setItemId(Integer itemId)             { this.itemId = itemId; }
    public void setName(String name)                  { this.name = name; }
    public void setPrice(Double price)                { this.price = price; }
    public void setCategory(MenuCategoryDto category) { this.category = category; }
    public void setIsAvailable(Boolean isAvailable)   { this.isAvailable = isAvailable; }
    public void setRecipeItems(List<RecipeItemDto> recipeItems) { this.recipeItems = recipeItems; }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}
