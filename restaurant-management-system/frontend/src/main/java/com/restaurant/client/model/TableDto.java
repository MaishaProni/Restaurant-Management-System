package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableDto {
    private Integer tableId;
    private Integer tableNumber;
    private Integer capacity;
    private String status;

    public Integer getTableId()     { return tableId; }
    public Integer getTableNumber() { return tableNumber; }
    public Integer getCapacity()    { return capacity; }
    public String  getStatus()      { return status; }

    public void setTableId(Integer tableId)         { this.tableId = tableId; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }
    public void setCapacity(Integer capacity)       { this.capacity = capacity; }
    public void setStatus(String status)            { this.status = status; }

    @Override
    public String toString() {
        return "Table " + tableNumber + " [" + status + "]";
    }
}
