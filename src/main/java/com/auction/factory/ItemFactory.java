package com.auction.factory;

import com.auction.model.Art;
import com.auction.model.Electronics;
import com.auction.model.Item;
import com.auction.model.Product;
import com.auction.model.Vehicle;

import java.time.LocalDateTime;

/**
 * Factory Method Pattern - tao dung subclass Item theo category.
 */
public class ItemFactory {

    /** Tao Item theo category (dung cho form them san pham). */
    public static Item create(String category, String name, String description,
                              double startPrice, double buyNowPrice,
                              LocalDateTime endTime, int sellerId) {
        return switch (category) {
            case "Electronics" ->
                    new Electronics(name, description, startPrice,
                            buyNowPrice, endTime, sellerId, "", 12);
            case "Art" ->
                    new Art(name, description, startPrice,
                            buyNowPrice, endTime, sellerId, "", 0);
            case "Vehicle" ->
                    new Vehicle(name, description, startPrice,
                            buyNowPrice, endTime, sellerId, "", 0, 0);
            default ->
                    new Product(name, description, startPrice,
                            buyNowPrice, endTime, sellerId);
        };
    }

    /** Tao Electronics voi thong tin nguoi ban tu nhap. */
    public static Electronics createElectronics(String name, String description,
                                                double startPrice, double buyNowPrice, LocalDateTime endTime,
                                                int sellerId, String brand, int warrantyMonths) {
        return new Electronics(name, description, startPrice,
                buyNowPrice, endTime, sellerId, brand, warrantyMonths);
    }

    /** Tao Art voi thong tin nguoi ban tu nhap. */
    public static Art createArt(String name, String description,
                                double startPrice, double buyNowPrice, LocalDateTime endTime,
                                int sellerId, String artist, int yearCreated) {
        return new Art(name, description, startPrice,
                buyNowPrice, endTime, sellerId, artist, yearCreated);
    }

    /** Tao Vehicle voi thong tin nguoi ban tu nhap. */
    public static Vehicle createVehicle(String name, String description,
                                        double startPrice, double buyNowPrice, LocalDateTime endTime,
                                        int sellerId, String manufacturer, int year, int mileage) {
        return new Vehicle(name, description, startPrice,
                buyNowPrice, endTime, sellerId, manufacturer, year, mileage);
    }
}