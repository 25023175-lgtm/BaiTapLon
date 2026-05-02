package com.uet.auction;

import com.auction.model.User;
import com.auction.model.Product;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest {

    @BeforeEach
    void cleanUp() {
        new File("users.dat").delete(); // Xóa file trước mỗi test để không bị ảnh hưởng
    }

    @AfterEach
    void tearDown() {
        new File("users.dat").delete(); // Dọn dẹp sau mỗi test
    }

    // ✅ Cover loadUsers() nhánh file không tồn tại
    @Test
    void testLoadUsers_WhenFileNotExists_ReturnsEmptyList() {
        List<User> users = DataManager.loadUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    // ✅ Cover saveUser() + loadUsers() nhánh bình thường
    @Test
    void testSaveAndLoadUser() {
        User testUser = new User("tester", "123", "test@uet.vn", "Tester UET", "Bidder");
        DataManager.saveUser(testUser);

        List<User> users = DataManager.loadUsers();
        assertFalse(users.isEmpty());
        assertTrue(users.get(users.size() - 1).getId() >= 1);
        assertEquals("tester", users.get(users.size() - 1).getUsername());
    }

    // ✅ Cover saveUser() ID tự tăng khi đã có user trước
    @Test
    void testSaveUser_SecondUser_GetsIncrementedId() {
        User user1 = new User("user1", "123", "u1@uet.vn", "User One", "Bidder");
        User user2 = new User("user2", "456", "u2@uet.vn", "User Two", "Bidder");
        DataManager.saveUser(user1);
        DataManager.saveUser(user2);

        List<User> users = DataManager.loadUsers();
        assertEquals(2, users.size());
        assertEquals(1, users.get(0).getId());
        assertEquals(2, users.get(1).getId());
    }

    // ✅ Cover loadProducts() nhánh catch (server không chạy)
    @Test
    void testLoadProducts_WhenServerDown_ReturnsEmptyList() {
        List<Product> products = DataManager.loadProducts();
        assertNotNull(products);
        assertEquals(0, products.size());
    }

    // ✅ Cover saveProducts() nhánh catch (server không chạy)
    @Test
    void testSaveProducts_WhenServerDown_DoesNotThrow() {
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        assertDoesNotThrow(() -> DataManager.saveProducts(products));
    }

    // ✅ Cover startRealtimeListener() — chỉ cần gọi để cover dòng khởi tạo thread
    @Test
    void testStartRealtimeListener_DoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() -> DataManager.startRealtimeListener(null));
        Thread.sleep(200); // Chờ thread chạy qua dòng kết nối rồi vào catch
    }
}