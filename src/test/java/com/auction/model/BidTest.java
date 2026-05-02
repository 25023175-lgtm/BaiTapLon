package com.auction.model;

import com.auction.model.Bid;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BidTest {

    // Kịch bản 1: Test Constructor khi người dùng đặt giá
    @Test
    void testParameterizedConstructor() {
        // ID sản phẩm: 10, ID người mua: 55, Giá đặt: 1.500.000
        Bid bid = new Bid(10, 55, 1500000.0);

        // Kiểm tra xem dữ liệu nhét vào có chuẩn không
        assertEquals(10, bid.getProductId());
        assertEquals(55, bid.getBidderId());
        assertEquals(1500000.0, bid.getAmount());

        // Kiểm tra logic mặc định
        assertFalse(bid.isAutoBid(), "Mặc định tính năng AutoBid phải đang tắt (false)!");
        assertNotNull(bid.getBidTime(), "Thời gian đặt giá (bidTime) không được để trống!");
    }

    // Kịch bản 2: Vét sạch các Getters và Setters còn lại
    @Test
    void testAllGettersAndSetters() {
        Bid bid = new Bid(); // Khởi tạo rỗng
        LocalDateTime timeNow = LocalDateTime.now();

        // Nhồi dữ liệu vào
        bid.setId(1);
        bid.setProductId(99);
        bid.setBidderId(42);
        bid.setBidderName("Duy Hưng");
        bid.setAmount(2000000.0);
        bid.setBidTime(timeNow);
        bid.setAutoBid(true);           // Bật tính năng đấu giá tự động
        bid.setMaxAutoBidAmount(5000000.0); // Cài mức giá trần tự động là 5 củ

        // Lấy ra kiểm tra
        assertEquals(1, bid.getId());
        assertEquals(99, bid.getProductId());
        assertEquals(42, bid.getBidderId());
        assertEquals("Duy Hưng", bid.getBidderName());
        assertEquals(2000000.0, bid.getAmount());
        assertEquals(timeNow, bid.getBidTime());
        assertTrue(bid.isAutoBid());    // Kiểm tra xem đã bật true chưa
        assertEquals(5000000.0, bid.getMaxAutoBidAmount());
    }

    // Kịch bản 3: Test hàm toString
    @Test
    void testToString() {
        Bid bid = new Bid();
        bid.setId(7);
        bid.setAmount(3500000.0);
        bid.setBidderId(15);

        String expectedString = "Bid{id=7, amount=3500000.0, bidder=15}";
        assertEquals(expectedString, bid.toString());
    }
}