package com.auction.common; // Hoặc com.uet.auction.common

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuctionClosedExceptionTest {

    @Test
    void testExceptionMessage() {
        String errorMessage = "Phiên đấu giá này đã đóng cửa, cấm ra giá!";

        AuctionClosedException exception = new AuctionClosedException(errorMessage);

        assertEquals(errorMessage, exception.getMessage(), "Thông báo lỗi bị sai lệch!");
    }
}