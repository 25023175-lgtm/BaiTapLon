package com.auction.model;

import java.time.LocalDateTime;

/** Xe co gioi - gia dat phai cao hon gia hien tai it nhat 500,000 VND. */
public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;
    private String manufacturer;
    private int year;
    private int mileage;

    public Vehicle() { super(); }

    public Vehicle(String name, String description, double startPrice,
                   double buyNowPrice, LocalDateTime endTime, int sellerId,
                   String manufacturer, int year, int mileage) {
        super(name, description, startPrice, buyNowPrice, endTime, sellerId);
        this.manufacturer = manufacturer;
        this.year = year;
        this.mileage = mileage;
    }

    @Override
    public String getCategory() { return "Vehicle"; }

    @Override
    public String getCategoryDescription() {
        return "Xe: " + manufacturer + " " + year
                + " - " + mileage + " km";
    }

    @Override
    public boolean isEligibleForBid(double bidAmount) {
        // Buoc gia toi thieu 500,000 VND
        return bidAmount >= getCurrentPrice() + 500_000;
    }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
}