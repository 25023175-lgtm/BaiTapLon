package com.uet.auction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainApplicationTest {

    // ✅ Chỉ test class tồn tại — không cần khởi tạo JavaFX
    @Test
    void testMainApplicationClassExists() {
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("com.uet.auction.MainApplication");
            assertNotNull(clazz);
        });
    }

    // ✅ Cover constructor không throw exception
    @Test
    void testAppInitialization() {
        assertDoesNotThrow(() -> {
            MainApplication app = new MainApplication();
            assertNotNull(app);
        });
    }
}