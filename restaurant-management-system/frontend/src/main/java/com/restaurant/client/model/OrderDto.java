package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {
    private Integer orderId;
    private TableDto table;
    private UserDto waiter;
    private String status;
    private String createdAt;
    private List<OrderItemDto> items;

    public Integer          getOrderId()  { return orderId; }
    public TableDto         getTable()    { return table; }
    public UserDto          getWaiter()   { return waiter; }
    public String           getStatus()   { return status; }
    public String           getCreatedAt(){ return createdAt; }
    public List<OrderItemDto> getItems()  { return items; }

    public void setOrderId(Integer orderId)           { this.orderId = orderId; }
    public void setTable(TableDto table)              { this.table = table; }
    public void setWaiter(UserDto waiter)             { this.waiter = waiter; }
    public void setStatus(String status)              { this.status = status; }
    public void setCreatedAt(String createdAt)        { this.createdAt = createdAt; }
    public void setItems(List<OrderItemDto> items)    { this.items = items; }

    public double getTotal() {
        if (items == null) return 0;
        return items.stream().mapToDouble(OrderItemDto::getSubtotal).sum();
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - Table " +
               (table != null ? table.getTableNumber() : "?") + " [" + status + "]";
    }
}
