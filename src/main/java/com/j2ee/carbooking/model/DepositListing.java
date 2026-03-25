package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.DepositListingStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "depositListings")
public class DepositListing {

    @Id
    private String id;
    private String sellerId;
    private String orderId;
    private String vehicleId;
    private Double originalDeposit;
    private Double sellingPrice;
    private Double platformFee;
    private LocalDateTime expiredAt;
    private DepositListingStatus status = DepositListingStatus.OPEN;
    private String buyerId;
    private LocalDateTime soldAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public Double getOriginalDeposit() { return originalDeposit; }
    public void setOriginalDeposit(Double originalDeposit) { this.originalDeposit = originalDeposit; }
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
    public Double getPlatformFee() { return platformFee; }
    public void setPlatformFee(Double platformFee) { this.platformFee = platformFee; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public DepositListingStatus getStatus() { return status; }
    public void setStatus(DepositListingStatus status) { this.status = status; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}