package com.auction.model;

import java.time.LocalDateTime;

/** Tac pham nghe thuat - gia dat phai vuot it nhat 5% gia hien tai. */
public class Art extends Item {
    private static final long serialVersionUID = 1L;
    private String artist;
    private int yearCreated;

    public Art() { super(); }

    public Art(String name, String description, double startPrice,
               double buyNowPrice, LocalDateTime endTime,
               int sellerId, String artist, int yearCreated) {
        super(name, description, startPrice, buyNowPrice, endTime, sellerId);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public String getCategory() { return "Art"; }

    @Override
    public String getCategoryDescription() {
        return "Nghe thuat: " + artist + " (" + yearCreated + ")";
    }

    @Override
    public boolean isEligibleForBid(double bidAmount) {
        // Do hiem: gia phai cao hon it nhat 5%
        return bidAmount > getCurrentPrice() * 1.05;
    }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public int getYearCreated() { return yearCreated; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }
}