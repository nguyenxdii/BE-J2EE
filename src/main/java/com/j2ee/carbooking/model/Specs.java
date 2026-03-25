package com.j2ee.carbooking.model;

public class Specs {
    private String engine;
    private String fuelType;
    private String transmission;
    private Integer seats;

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
}