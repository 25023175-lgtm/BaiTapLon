package com.auction.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/** Test cho Admin, Seller, Bidder, Art, Electronics, Vehicle */
class RoleTest {

    // ── ADMIN ────────────────────────────────────────────────────────
    @Test
    void testAdminHasAllPermissions() {
        Admin admin = new Admin("admin", "pass", "a@a.com", "Admin");
        assertTrue(admin.hasPermission("PLACE_BID"));
        assertTrue(admin.hasPermission("ADD_PRODUCT"));
        assertTrue(admin.hasPermission("DELETE_ANY"));
        assertTrue(admin.hasPermission("BAT_KY_QUYEN_NAO"));
        assertEquals("Admin", admin.getRole());
        assertFalse(admin.getPermissionDescription().isEmpty());
    }

    @Test
    void testAdminGettersSetters() {
        Admin admin = new Admin();
        admin.setId(1);
        admin.setUsername("superadmin");
        admin.setEmail("admin@sys.com");
        assertEquals(1, admin.getId());
        assertEquals("superadmin", admin.getUsername());
        assertEquals("admin@sys.com", admin.getEmail());
    }

    // ── SELLER ───────────────────────────────────────────────────────
    @Test
    void testSellerPermissions() {
        Seller seller = new Seller("seller1", "pass", "s@s.com", "Seller One");
        assertTrue(seller.hasPermission("ADD_PRODUCT"));
        assertTrue(seller.hasPermission("DELETE_OWN_PRODUCT"));
        assertFalse(seller.hasPermission("PLACE_BID"));
        assertEquals("Seller", seller.getRole());
        assertFalse(seller.getPermissionDescription().isEmpty());
    }

    @Test
    void testSellerGettersSetters() {
        Seller seller = new Seller();
        seller.setId(2);
        seller.setFullName("Nguyen Van Ban");
        seller.setBalance(1_000_000);
        assertEquals(2, seller.getId());
        assertEquals("Nguyen Van Ban", seller.getFullName());
        assertEquals(1_000_000, seller.getBalance());
    }

    // ── ART ──────────────────────────────────────────────────────────
    @Test
    void testArtGettersSetters() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Art art = new Art("Tranh Son Dau", "Dep", 10_000_000, 0, end, 1,
                "Van Gogh", 1889);
        assertEquals("Van Gogh", art.getArtist());
        assertEquals(1889, art.getYearCreated());
        assertEquals("Art", art.getCategory());
        assertTrue(art.getCategoryDescription().contains("Van Gogh"));

        art.setArtist("Picasso");
        art.setYearCreated(1950);
        assertEquals("Picasso", art.getArtist());
        assertEquals(1950, art.getYearCreated());
    }

    @Test
    void testArtBidRule() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Art art = new Art("Tranh", "Mo ta", 10_000_000, 0, end, 1, "A", 2000);
        // Phai cao hon 5%
        assertFalse(art.isEligibleForBid(10_400_000)); // < 5%
        assertTrue(art.isEligibleForBid(10_600_000));  // > 5%
    }

    // ── ELECTRONICS ──────────────────────────────────────────────────
    @Test
    void testElectronicsGettersSetters() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Electronics e = new Electronics("Laptop", "Mo ta", 20_000_000, 0,
                end, 1, "Dell", 12);
        assertEquals("Dell", e.getBrand());
        assertEquals(12, e.getWarrantyMonths());
        assertEquals("Electronics", e.getCategory());
        assertTrue(e.getCategoryDescription().contains("Dell"));

        e.setBrand("HP");
        e.setWarrantyMonths(24);
        assertEquals("HP", e.getBrand());
        assertEquals(24, e.getWarrantyMonths());
    }

    @Test
    void testElectronicsBidRule() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Electronics e = new Electronics("Phone", "Mo ta", 10_000_000, 0,
                end, 1, "Samsung", 6);
        // Phai cao hon 1%
        assertFalse(e.isEligibleForBid(10_050_000)); // < 1%
        assertTrue(e.isEligibleForBid(10_200_000));  // > 1%
    }

    // ── VEHICLE ──────────────────────────────────────────────────────
    @Test
    void testVehicleGettersSetters() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Vehicle v = new Vehicle("Xe SH", "Mo ta", 80_000_000, 0,
                end, 1, "Honda", 2021, 15000);
        assertEquals("Honda", v.getManufacturer());
        assertEquals(2021, v.getYear());
        assertEquals(15000, v.getMileage());
        assertEquals("Vehicle", v.getCategory());
        assertTrue(v.getCategoryDescription().contains("Honda"));

        v.setManufacturer("Yamaha");
        v.setYear(2023);
        v.setMileage(5000);
        assertEquals("Yamaha", v.getManufacturer());
        assertEquals(2023, v.getYear());
        assertEquals(5000, v.getMileage());
    }

    @Test
    void testVehicleBidRule() {
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Vehicle v = new Vehicle("Xe", "Mo ta", 30_000_000, 0,
                end, 1, "Honda", 2020, 10000);
        // Phai cao hon it nhat 500_000
        assertFalse(v.isEligibleForBid(30_400_000)); // < 500k
        assertTrue(v.isEligibleForBid(30_500_000));  // == 500k
        assertTrue(v.isEligibleForBid(31_000_000));  // > 500k
    }
}