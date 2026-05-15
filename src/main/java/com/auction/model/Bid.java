package com.auction.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Bid implements Serializable {
    private int id;
    private int productId;
    private int bidderId;
    private String bidderName;
    private double amount;
    private LocalDateTime bidTime;
    private boolean autoBid;
    private double maxAutoBidAmount;

    public Bid() {}

    public Bid(int productId, int bidderId, double amount) {
        this.productId = productId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = LocalDateTime.now();
        this.autoBid = false;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBidderId() { return bidderId; }
    public void setBidderId(int bidderId) { this.bidderId = bidderId; }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    public boolean isAutoBid() { return autoBid; }
    public void setAutoBid(boolean autoBid) { this.autoBid = autoBid; }

    public double getMaxAutoBidAmount() { return maxAutoBidAmount; }
    public void setMaxAutoBidAmount(double maxAutoBidAmount) { this.maxAutoBidAmount = maxAutoBidAmount; }

    @Override
    public String toString() {
        return "Bid{id=" + id + ", amount=" + amount + ", bidder=" + bidderId + "}";
    }
}