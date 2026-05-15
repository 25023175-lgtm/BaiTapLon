package com.auction.model;

import java.time.LocalDateTime;

/** San pham dien tu - yeu cau gia dat toi thieu tang 1% so voi gia hien tai. */
public class Electronics extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;
    private int warrantyMonths;

    public Electronics() { super(); }

    public Electronics(String name, String description, double startPrice,
                       double buyNowPrice, LocalDateTime endTime,
                       int sellerId, String brand, int warrantyMonths) {
        super(name, description, startPrice, buyNowPrice, endTime, sellerId);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getCategory() { return "Electronics"; }

    @Override
    public String getCategoryDescription() {
        return "Dien tu: " + brand + " - Bao hanh " + warrantyMonths + " thang";
    }

    @Override
    public boolean isEligibleForBid(double bidAmount) {
        // Gia phai cao hon it nhat 1% so voi gia hien tai
        return bidAmount > getCurrentPrice() * 1.01;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}