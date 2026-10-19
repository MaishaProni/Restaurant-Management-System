package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuCategoryDto {
    private Integer categoryId;
    private String name;

    public Integer getCategoryId() { return categoryId; }
    public String  getName()       { return name; }

    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public void setName(String name)              { this.name = name; }

    @Override
    public String toString() { return name; }
}
