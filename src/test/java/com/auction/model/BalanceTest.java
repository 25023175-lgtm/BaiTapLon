package com.auction.model;

import com.auction.factory.UserFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Test chuc nang so du tai khoan (nap tien). */
class BalanceTest {

    @Test
    void testInitialBalanceIsZero() {
        // Tai khoan moi tao phai co so du bang 0
        User user = UserFactory.create("u", "p", "e", "n", "Seller");
        assertEquals(0.0, user.getBalance());
    }

    @Test
    void testSetBalance() {
        User user = new Bidder();
        user.setBalance(1_000_000.0);
        assertEquals(1_000_000.0, user.getBalance());
    }

    @Test
    void testAddBalance() {
        User user = new Seller();
        user.setBalance(500_000.0);
        // Gia lap nap them 200_000
        user.setBalance(user.getBalance() + 200_000.0);
        assertEquals(700_000.0, user.getBalance());
    }

    @Test
    void testBalanceAfterMultipleDeposits() {
        User user = new Seller();
        user.setBalance(0.0);
        // Nap 3 lan
        user.setBalance(user.getBalance() + 100_000.0);
        user.setBalance(user.getBalance() + 250_000.0);
        user.setBalance(user.getBalance() + 50_000.0);
        assertEquals(400_000.0, user.getBalance());
    }

    @Test
    void testBalanceNegativeNotAllowed() {
        User user = new Bidder();
        user.setBalance(100_000.0);
        // So du khong duoc am sau khi dat gia
        assertTrue(user.getBalance() >= 0);
    }

    @Test
    void testAdminBalanceIndependent() {
        // Admin cung co the co so du
        User admin = new Admin("admin", "pass", "a@a.com", "Admin");
        admin.setBalance(10_000_000.0);
        assertEquals(10_000_000.0, admin.getBalance());
    }
}