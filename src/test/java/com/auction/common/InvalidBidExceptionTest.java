package com.auction.common; // Hoặc com.uet.auction.common (tùy vào package của bạn)

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidBidExceptionTest {

    @Test
    void testExceptionMessage() {
        // Tạo một câu thông báo lỗi giả định
        String errorMessage = "Giá đưa ra phải lớn hơn giá hiện tại!";

        // Khởi tạo cục lỗi với câu thông báo đó
        InvalidBidException exception = new InvalidBidException(errorMessage);

        // Kiểm tra xem lúc lấy lỗi ra (getMessage), nó có giữ nguyên câu thông báo không
        assertEquals(errorMessage, exception.getMessage(), "Thông báo lỗi bị sai lệch!");
    }
}