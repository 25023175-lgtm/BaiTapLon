package com.auction.model;

import com.auction.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // Kịch bản 1: Test Constructor có truyền tham số
    @Test
    void testParameterizedConstructor() {
        // Đóng vai người dùng mới đăng ký tài khoản
        User user = new User("duyhung2411", "123456", "hung@gmail.com", "Đặng Duy Hưng", "Seller");

        // Kiểm tra xem hệ thống có lưu đúng dữ liệu không
        assertEquals("duyhung2411", user.getUsername());
        assertEquals("123456", user.getPassword());
        assertEquals("hung@gmail.com", user.getEmail());
        assertEquals("Đặng Duy Hưng", user.getFullName());
        assertEquals("Seller", user.getRole());

        // Kiểm tra logic mặc định: Tài khoản mới tạo thì số dư phải bằng 0
        assertEquals(0.0, user.getBalance());
    }

    // Kịch bản 2: Vét sạch toàn bộ Setters và Getters còn lại
    @Test
    void testGettersAndSetters() {
        User user = new User(); // Dùng Constructor rỗng

        // Set dữ liệu
        user.setId(101);
        user.setUsername("bidder01");
        user.setPassword("password123");
        user.setEmail("bidder@test.com");
        user.setFullName("Nguyễn Văn A");
        user.setBalance(500000.0); // Nạp 500k vào tài khoản

        // Lấy ra đối chiếu
        assertEquals(101, user.getId());
        assertEquals("bidder01", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("bidder@test.com", user.getEmail());
        assertEquals("Nguyễn Văn A", user.getFullName());
        assertEquals(500000.0, user.getBalance());
    }

    // Kịch bản 3: Test hàm toString
    @Test
    void testToString() {
        User user = new User();
        user.setId(5);
        user.setUsername("admin_hung");

        String expectedString = "User{id=5, username='admin_hung'}";
        assertEquals(expectedString, user.toString());
    }
}