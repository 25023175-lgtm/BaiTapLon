package com.auction.model;

import com.auction.factory.UserFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // Kich ban 1: Test Constructor co truyen tham so
    // User la abstract -> dung UserFactory de tao Seller
    @Test
    void testParameterizedConstructor() {
        User user = UserFactory.create(
                "duyhung2411", "123456",
                "hung@gmail.com", "Dang Duy Hung", "Seller");

        assertEquals("duyhung2411", user.getUsername());
        assertEquals("123456", user.getPassword());
        assertEquals("hung@gmail.com", user.getEmail());
        assertEquals("Dang Duy Hung", user.getFullName());
        assertEquals("Seller", user.getRole());

        // Tai khoan moi tao thi so du phai bang 0
        assertEquals(0.0, user.getBalance());
    }

    // Kich ban 2: Test Setters va Getters
    // Dung Bidder (subclass cu the) thay vi new User()
    @Test
    void testGettersAndSetters() {
        User user = new Bidder(); // Bidder extends User

        user.setId(101);
        user.setUsername("bidder01");
        user.setPassword("password123");
        user.setEmail("bidder@test.com");
        user.setFullName("Nguyen Van A");
        user.setBalance(500000.0);

        assertEquals(101, user.getId());
        assertEquals("bidder01", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("bidder@test.com", user.getEmail());
        assertEquals("Nguyen Van A", user.getFullName());
        assertEquals(500000.0, user.getBalance());
    }

    // Kich ban 3: Test toString
    @Test
    void testToString() {
        User user = new Bidder();
        user.setId(5);
        user.setUsername("admin_hung");

        String result = user.toString();
        assertTrue(result.contains("5"));
        assertTrue(result.contains("admin_hung"));
    }
}