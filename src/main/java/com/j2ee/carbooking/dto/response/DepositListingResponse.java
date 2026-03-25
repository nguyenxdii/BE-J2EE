package com.j2ee.carbooking.dto.response;

import com.j2ee.carbooking.enums.DepositListingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DepositListingResponse {
    private String id;
    private String vehicleId;
    private String vehicleName;
    private String vehicleImage;
    private String vehicleBrand;
    private String orderId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private Double originalDeposit;
    private Double sellingPrice;
    private Double savedAmount;
    private DepositListingStatus status;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getVehicleImage() { return vehicleImage; }
    public void setVehicleImage(String vehicleImage) { this.vehicleImage = vehicleImage; }
    public String getVehicleBrand() { return vehicleBrand; }
    public void setVehicleBrand(String vehicleBrand) { this.vehicleBrand = vehicleBrand; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    public Double getOriginalDeposit() { return originalDeposit; }
    public void setOriginalDeposit(Double originalDeposit) { this.originalDeposit = originalDeposit; }
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
    public Double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(Double savedAmount) { this.savedAmount = savedAmount; }
    public DepositListingStatus getStatus() { return status; }
    public void setStatus(DepositListingStatus status) { this.status = status; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
