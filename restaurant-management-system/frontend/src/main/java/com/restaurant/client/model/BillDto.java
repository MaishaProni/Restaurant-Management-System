package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillDto {
    private Integer billId;
    private Double subtotal;
    private Double taxAmount;
    private Double serviceCharge;
    private Double grandTotal;
    private String generatedAt;

    public Integer getBillId()        { return billId; }
    public Double  getSubtotal()      { return subtotal; }
    public Double  getTaxAmount()     { return taxAmount; }
    public Double  getServiceCharge() { return serviceCharge; }
    public Double  getGrandTotal()    { return grandTotal; }
    public String  getGeneratedAt()   { return generatedAt; }

    public void setBillId(Integer billId)              { this.billId = billId; }
    public void setSubtotal(Double subtotal)           { this.subtotal = subtotal; }
    public void setTaxAmount(Double taxAmount)         { this.taxAmount = taxAmount; }
    public void setServiceCharge(Double serviceCharge) { this.serviceCharge = serviceCharge; }
    public void setGrandTotal(Double grandTotal)       { this.grandTotal = grandTotal; }
    public void setGeneratedAt(String generatedAt)     { this.generatedAt = generatedAt; }
}
