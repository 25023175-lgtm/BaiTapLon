package com.auction.factory;

import com.auction.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {

    private final LocalDateTime end = LocalDateTime.now().plusHours(2);

    @Test
    void testCreateElectronics() {
        Item item = ItemFactory.create("Electronics", "Laptop", "Mo ta",
                10_000_000, 0, end, 1);
        assertInstanceOf(Electronics.class, item);
        assertEquals("Electronics", item.getCategory());
        assertEquals("Laptop", item.getName());
        assertEquals(10_000_000, item.getStartPrice());
    }

    @Test
    void testCreateArt() {
        Item item = ItemFactory.create("Art", "Tranh", "Mo ta",
                5_000_000, 0, end, 1);
        assertInstanceOf(Art.class, item);
        assertEquals("Art", item.getCategory());
    }

    @Test
    void testCreateVehicle() {
        Item item = ItemFactory.create("Vehicle", "Xe may", "Mo ta",
                30_000_000, 0, end, 1);
        assertInstanceOf(Vehicle.class, item);
        assertEquals("Vehicle", item.getCategory());
    }

    @Test
    void testCreateGeneral() {
        Item item = ItemFactory.create("General", "Do linh tinh", "Mo ta",
                100_000, 0, end, 1);
        assertInstanceOf(Product.class, item);
        assertEquals("General", item.getCategory());
    }

    @Test
    void testCreateDefault() {
        Item item = ItemFactory.create("KhongBiet", "Ten", "Mo ta",
                100_000, 0, end, 1);
        assertInstanceOf(Product.class, item);
    }

    @Test
    void testCreateElectronicsSpecific() {
        Electronics e = ItemFactory.createElectronics("iPhone", "Mo ta",
                20_000_000, 0, end, 1, "Apple", 24);
        assertEquals("Apple", e.getBrand());
        assertEquals(24, e.getWarrantyMonths());
        assertTrue(e.getCategoryDescription().contains("Apple"));
    }

    @Test
    void testCreateArtSpecific() {
        Art a = ItemFactory.createArt("Tranh Picasso", "Mo ta",
                50_000_000, 0, end, 1, "Picasso", 1950);
        assertEquals("Picasso", a.getArtist());
        assertEquals(1950, a.getYearCreated());
        assertTrue(a.getCategoryDescription().contains("Picasso"));
    }

    @Test
    void testCreateVehicleSpecific() {
        Vehicle v = ItemFactory.createVehicle("Wave Alpha", "Mo ta",
                25_000_000, 0, end, 1, "Honda", 2022, 5000);
        assertEquals("Honda", v.getManufacturer());
        assertEquals(2022, v.getYear());
        assertEquals(5000, v.getMileage());
        assertTrue(v.getCategoryDescription().contains("Honda"));
    }
}