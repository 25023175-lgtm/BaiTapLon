package com.auction.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void testExceptionMessage() {
        AuthenticationException ex = new AuthenticationException("Sai mat khau");
        assertEquals("Sai mat khau", ex.getMessage());
    }

    @Test
    void testExceptionIsChecked() {
        assertInstanceOf(Exception.class, new AuthenticationException("test"));
    }
}