package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    private Electronics electronics;
    private Art art;
    private Vehicle vehicle;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        endTime = LocalDateTime.now().plusHours(2);
        electronics = new Electronics("Laptop", "Mo ta", 10_000_000, 0,
                endTime, 1, "Dell", 12);
        art = new Art("Tranh", "Mo ta", 5_000_000, 0,
                endTime, 1, "Picasso", 2000);
        vehicle = new Vehicle("Xe may", "Mo ta", 30_000_000, 0,
                endTime, 1, "Honda", 2020, 10000);
    }

    @Test
    void testCategory() {
        assertEquals("Electronics", electronics.getCategory());
        assertEquals("Art", art.getCategory());
        assertEquals("Vehicle", vehicle.getCategory());
    }

    @Test
    void testElectronicsBidRule() {
        // Gia electronics phai cao hon 1% gia hien tai
        double current = electronics.getCurrentPrice(); // 10_000_000
        assertFalse(electronics.isEligibleForBid(current));        // bang gia cu
        assertFalse(electronics.isEligibleForBid(current * 1.005)); // chua du 1%
        assertTrue(electronics.isEligibleForBid(current * 1.02));  // du 1%
    }

    @Test
    void testArtBidRule() {
        // Art phai cao hon 5%
        double current = art.getCurrentPrice(); // 5_000_000
        assertFalse(art.isEligibleForBid(current * 1.03));  // chua du 5%
        assertTrue(art.isEligibleForBid(current * 1.06));   // du 5%
    }

    @Test
    void testVehicleBidRule() {
        // Vehicle phai cao hon it nhat 500_000
        double current = vehicle.getCurrentPrice(); // 30_000_000
        assertFalse(vehicle.isEligibleForBid(current + 100_000));  // chua du
        assertTrue(vehicle.isEligibleForBid(current + 500_000));   // du
    }

    @Test
    void testPriceHistory() {
        double start = electronics.getCurrentPrice();
        electronics.addPriceToHistory(11_000_000);
        electronics.addPriceToHistory(12_000_000);
        assertEquals(3, electronics.getPriceHistory().size());
        assertEquals(start, electronics.getPriceHistory().get(0));
        assertEquals(12_000_000, electronics.getPriceHistory().get(2));
    }

    @Test
    void testToStringContainsCategory() {
        String str = electronics.toString();
        assertTrue(str.contains("Electronics"));
    }
}