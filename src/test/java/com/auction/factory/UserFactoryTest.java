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
        // Admin co toan quyen
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
}