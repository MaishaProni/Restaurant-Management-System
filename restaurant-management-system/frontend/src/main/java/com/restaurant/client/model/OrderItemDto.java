package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemDto {
    private Integer orderItemId;
    private MenuItemDto menuItem;
    private Integer quantity;
    private Double unitPrice;
    private String notes;

    public Integer    getOrderItemId() { return orderItemId; }
    public MenuItemDto getMenuItem()   { return menuItem; }
    public Integer    getQuantity()    { return quantity; }
    public Double     getUnitPrice()   { return unitPrice; }
    public String     getNotes()       { return notes; }
    public Double     getSubtotal()    { return unitPrice != null && quantity != null ? unitPrice * quantity : 0; }

    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public void setMenuItem(MenuItemDto menuItem)   { this.menuItem = menuItem; }
    public void setQuantity(Integer quantity)       { this.quantity = quantity; }
    public void setUnitPrice(Double unitPrice)      { this.unitPrice = unitPrice; }
    public void setNotes(String notes)              { this.notes = notes; }
}
