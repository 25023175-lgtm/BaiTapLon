package com.auction.factory;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    @Test
    void testCreateBidder() {
        User user = UserFactory.create("u1", "P@ss1234", "a@a.com", "Nguyen A", "Bidder");
        assertInstanceOf(Bidder.class, user);
        assertEquals("Bidder", user.getRole());
        assertTrue(user.hasPermission("PLACE_BID"));
        assertFalse(user.hasPermission("ADD_PRODUCT"));
    }

    @Test
    void testCreateSeller() {
        User user = UserFactory.create("u2", "P@ss1234", "b@b.com", "Tran B", "Seller");
        assertInstanceOf(Seller.class, user);
        assertTrue(user.hasPermission("ADD_PRODUCT"));
        assertFalse(user.hasPermission("PLACE_BID"));
    }

    @Test
    void testCreateAdmin() {
        User user = UserFactory.create("admin", "P@ss1234", "c@c.com", "Admin C", "Admin");
        assertInstanceOf(Admin.class, user);
        assertTrue(user.hasPermission("ADD_PRODUCT"));
        assertTrue(user.hasPermission("PLACE_BID"));
        assertTrue(user.hasPermission("DELETE_ANY"));
    }

    @Test
    void testInvalidRoleThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.create("u", "p", "e", "n", "InvalidRole"));
    }

    @Test
    void testPermissionDescription() {
        User bidder = UserFactory.create("u", "p", "e", "n", "Bidder");
        assertNotNull(bidder.getPermissionDescription());
        assertFalse(bidder.getPermissionDescription().isEmpty());
    }

    // ── Test normalizeRole: role viet thuong/hoa van hoat dong ────────
    @Test
    void testNormalizeRoleLowercase() {
        User user = UserFactory.create("u", "p", "e", "n", "bidder");
        assertInstanceOf(Bidder.class, user);
        assertEquals("Bidder", user.getRole());
    }

    @Test
    void testNormalizeRoleUppercase() {
        User user = UserFactory.create("u", "p", "e", "n", "SELLER");
        assertInstanceOf(Seller.class, user);
        assertEquals("Seller", user.getRole());
    }

    @Test
    void testNormalizeRoleMixedCase() {
        User user = UserFactory.create("u", "p", "e", "n", "aDmIn");
        assertInstanceOf(Admin.class, user);
        assertEquals("Admin", user.getRole());
    }

    // ── Test createFromDb: id va balance duoc gan dung ───────────────
    @Test
    void testCreateFromDb() {
        User user = UserFactory.createFromDb(
                42, "hung", "pass", "hung@uet.vn", "Dang Duy Hung",
                "Seller", 500_000.0);

        assertInstanceOf(Seller.class, user);
        assertEquals(42, user.getId());
        assertEquals("hung", user.getUsername());
        assertEquals("Dang Duy Hung", user.getFullName());
        assertEquals("Seller", user.getRole());
        assertEquals(500_000.0, user.getBalance());
    }

    @Test
    void testCreateFromDbInvalidRoleFallback() {
        // Role la trong DB -> fallback ve Bidder, khong nem exception
        User user = UserFactory.createFromDb(
                1, "u", "p", "e", "n", "RoleKhongTonTai", 0.0);
        assertInstanceOf(Bidder.class, user);
        assertEquals(1, user.getId());
    }
}