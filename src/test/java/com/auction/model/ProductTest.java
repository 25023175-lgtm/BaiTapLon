package com.auction.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    // Kich ban 1: Test Constructor co truyen tham so
    @Test
    void testParameterizedConstructor() {
        LocalDateTime endTime = LocalDateTime.now().plusDays(3);

        Product product = new Product(
                "iPhone 15 Pro", "Hang luot 99%",
                20000000.0, 25000000.0, endTime, 101);

        assertEquals("iPhone 15 Pro", product.getName());
        assertEquals("Hang luot 99%", product.getDescription());
        assertEquals(20000000.0, product.getStartPrice());
        assertEquals(20000000.0, product.getCurrentPrice());
        assertEquals(25000000.0, product.getBuyNowPrice());
        assertEquals(endTime, product.getEndTime());
        assertEquals("ACTIVE", product.getStatus());
        assertEquals(101, product.getSellerId());
        assertNotNull(product.getStartTime());

        // Product extends Item -> category phai la General
        assertEquals("General", product.getCategory());
    }

    // Kich ban 2: Test Setters va Getters
    @Test
    void testAllGettersAndSetters() {
        Product p = new Product();

        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime timeEnd = timeNow.plusDays(7);

        p.setId(1);
        p.setName("Laptop Gaming");
        p.setDescription("Bao chien max setting");
        p.setStartPrice(15000000.0);
        p.setCurrentPrice(16000000.0);
        p.setBuyNowPrice(20000000.0);
        p.setStartTime(timeNow);
        p.setEndTime(timeEnd);
        p.setStatus("ENDED");
        p.setSellerId(99);
        p.setSellerName("Duy Hung");

        assertEquals(1, p.getId());
        assertEquals("Laptop Gaming", p.getName());
        assertEquals("Bao chien max setting", p.getDescription());
        assertEquals(15000000.0, p.getStartPrice());
        assertEquals(16000000.0, p.getCurrentPrice());
        assertEquals(20000000.0, p.getBuyNowPrice());
        assertEquals(timeNow, p.getStartTime());
        assertEquals(timeEnd, p.getEndTime());
        assertEquals("ENDED", p.getStatus());
        assertEquals(99, p.getSellerId());
        assertEquals("Duy Hung", p.getSellerName());
    }

    // Kich ban 3: Test isEligibleForBid (Product phai cao hon gia hien tai)
    @Test
    void testIsEligibleForBid() {
        Product p = new Product();
        p.setCurrentPrice(10000000.0);

        assertFalse(p.isEligibleForBid(10000000.0)); // bang gia cu -> khong hop le
        assertFalse(p.isEligibleForBid(9000000.0));  // thap hon -> khong hop le
        assertTrue(p.isEligibleForBid(10000001.0));  // cao hon -> hop le
    }

    // Kich ban 4: Test toString chua format moi cua Item
    @Test
    void testToString() {
        Product p = new Product();
        p.setId(5);
        p.setName("Dong ho Rolex");
        p.setCurrentPrice(50000000.0);

        String result = p.toString();
        // toString() cua Item: "Item{id=5, name='...', category='General', price=...}"
        assertTrue(result.contains("5"));
        assertTrue(result.contains("Dong ho Rolex"));
        assertTrue(result.contains("General"));
    }
}