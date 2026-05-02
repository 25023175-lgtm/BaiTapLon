package com.auction.model;

import com.auction.model.Product; // Đảm bảo import đúng package Product của bạn
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    // Kịch bản 1: Test cái Constructor khổng lồ của bạn
    @Test
    void testParameterizedConstructor() {
        LocalDateTime endTime = LocalDateTime.now().plusDays(3);

        // Gọi thẳng vào cái Constructor có truyền tham số
        Product product = new Product("iPhone 15 Pro", "Hàng lướt 99%", 20000000.0, 25000000.0, endTime, 101);

        // Kiểm tra xem nó có "nhét" đúng dữ liệu vào bụng không
        assertEquals("iPhone 15 Pro", product.getName());
        assertEquals("Hàng lướt 99%", product.getDescription());
        assertEquals(20000000.0, product.getStartPrice());
        assertEquals(20000000.0, product.getCurrentPrice()); // Ban đầu giá hiện tại phải bằng giá khởi điểm
        assertEquals(25000000.0, product.getBuyNowPrice());
        assertEquals(endTime, product.getEndTime());
        assertEquals("ACTIVE", product.getStatus());
        assertEquals(101, product.getSellerId());
        assertNotNull(product.getStartTime(), "Thời gian bắt đầu không được để trống!");
    }

    // Kịch bản 2: Vét sạch toàn bộ các hàm Setters và Getters còn lại
    @Test
    void testAllGettersAndSetters() {
        Product p = new Product(); // Dùng Constructor rỗng

        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime timeEnd = timeNow.plusDays(7);

        // Nã liên thanh toàn bộ Setter
        p.setId(1);
        p.setName("Laptop Gaming");
        p.setDescription("Bao chiến max setting");
        p.setStartPrice(15000000.0);
        p.setCurrentPrice(16000000.0);
        p.setBuyNowPrice(20000000.0);
        p.setStartTime(timeNow);
        p.setEndTime(timeEnd);
        p.setStatus("ENDED");
        p.setSellerId(99);
        p.setSellerName("Duy Hưng");

        // Dùng Getter lôi ra đối chiếu
        assertEquals(1, p.getId());
        assertEquals("Laptop Gaming", p.getName());
        assertEquals("Bao chiến max setting", p.getDescription());
        assertEquals(15000000.0, p.getStartPrice());
        assertEquals(16000000.0, p.getCurrentPrice());
        assertEquals(20000000.0, p.getBuyNowPrice());
        assertEquals(timeNow, p.getStartTime());
        assertEquals(timeEnd, p.getEndTime());
        assertEquals("ENDED", p.getStatus());
        assertEquals(99, p.getSellerId());
        assertEquals("Duy Hưng", p.getSellerName());
    }

    // Kịch bản 3: Test nốt cái hàm toString ở dòng cuối cùng của bạn
    @Test
    void testToString() {
        Product p = new Product();
        p.setId(5);
        p.setName("Đồng hồ Rolex");
        p.setCurrentPrice(50000000.0);

        String expectedString = "Product{id=5, name='Đồng hồ Rolex', price=5.0E7}"; // 50 củ nó sẽ hiển thị dạng khoa học 5.0E7 nếu dùng double
        assertEquals(expectedString, p.toString());
    }
}