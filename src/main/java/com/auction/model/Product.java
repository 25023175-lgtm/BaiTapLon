package com.auction.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String description;
    private double startPrice;
    private double currentPrice;
    private double buyNowPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // ACTIVE, ENDED, CANCELLED
    private int sellerId;
    private String sellerName;
    private int bidCount;

    // Danh sách lưu lại các mốc giá, dùng để vẽ biểu đồ
    private java.util.List<Double> priceHistory = new java.util.ArrayList<>();



    public Product() {}

    public Product(String name, String description, double startPrice,
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

    public int getBidCount() {
        return bidCount;
    }
    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public java.util.List<Double> getPriceHistory() {
        // Nếu đọc đồ từ file cũ lên bị null -> Tự tạo danh sách mới
        if (this.priceHistory == null) {
            this.priceHistory = new java.util.ArrayList<>();
            this.priceHistory.add(this.startPrice); // Cắm mốc giá gốc vào đầu tiên
        }
        return priceHistory;
    }

    // Hàm này sẽ được gọi mỗi khi có người đặt giá thành công
    public void addPriceToHistory(double newPrice) {
        // Đề phòng trường hợp đồ cũ chưa có danh sách
        if (this.priceHistory == null) {
            this.priceHistory = new java.util.ArrayList<>();
            this.priceHistory.add(this.startPrice);
        }
        this.priceHistory.add(newPrice);
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + currentPrice + "}";
    }
}
