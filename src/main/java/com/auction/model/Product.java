package com.auction.model;

import java.time.LocalDateTime;

/**
 * San pham chung (loai mac dinh) ke thua Item.
 * Dung khi khong biet ro loai hang.
 */
public class Product extends Item {
    private static final long serialVersionUID = 1L;

    public Product() { super(); }

    public Product(String name, String description, double startPrice,
                   double buyNowPrice, LocalDateTime endTime, int sellerId) {
        super(name, description, startPrice, buyNowPrice, endTime, sellerId);
    }

    @Override
    public String getCategory() { return "General"; }

    @Override
    public String getCategoryDescription() {
        return "San pham pho thong";
    }

    @Override
    public boolean isEligibleForBid(double bidAmount) {
        return bidAmount > getCurrentPrice();
    }
}