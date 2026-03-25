package com.j2ee.carbooking.dto.response;

public class BuyDepositListingResponse {
    private String listingId;
    private String orderId;
    private String vehicleName;
    private Double paidAmount;
    private String payUrl;
    private String message;

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
