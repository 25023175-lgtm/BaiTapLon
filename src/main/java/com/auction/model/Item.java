package com.auction.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lop truu tuong dai dien cho san pham dau gia.
 * Cac loai cu the (Electronics, Art, Vehicle) ke thua lop nay.
 */
public abstract class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private double startPrice;
    private double currentPrice;
    private double buyNowPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private int sellerId;
    private String sellerName;
    private String sellerEmail;
    private int bidCount;
    private List<Double> priceHistory = new ArrayList<>();

    public Item() {}

    public Item(String name, String description, double startPrice,
                double buyNowPrice, LocalDateTime endTime, int sellerId) {
        this.name = name;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.startTime = LocalDateTime.now();
        this.endTime = endTime;
        this.status = "ACTIVE";
        this.sellerId = sellerId;
        this.bidCount = 0;
        this.priceHistory.add(startPrice);
    }

    /** Tra ve loai san pham - bat buoc override (tinh truu tuong). */
    public abstract String getCategory();

    /** Mo ta dac biet cua loai san pham - da hinh. */
    public abstract String getCategoryDescription();

    /** Kiem tra dieu kien dau gia rieng theo tung loai hang. */
    public abstract boolean isEligibleForBid(double bidAmount);

    public void addPriceToHistory(double newPrice) {
        if (this.priceHistory == null) {
            this.priceHistory = new ArrayList<>();
            this.priceHistory.add(this.startPrice);
        }
        this.priceHistory.add(newPrice);
    }

    public List<Double> getPriceHistory() {
        if (this.priceHistory == null) {
            this.priceHistory = new ArrayList<>();
            this.priceHistory.add(this.startPrice);
        }
        return priceHistory;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getStartPrice() { return startPrice; }
    public void setStartPrice(double startPrice) { this.startPrice = startPrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getBuyNowPrice() { return buyNowPrice; }
    public void setBuyNowPrice(double buyNowPrice) { this.buyNowPrice = buyNowPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public int getBidCount() { return bidCount; }
    public void setBidCount(int bidCount) { this.bidCount = bidCount; }

    @Override
    public String toString() {
        return "Item{id=" + id + ", name='" + name
                + "', category='" + getCategory()
                + "', price=" + currentPrice + "}";
    }
}