package com.uet.auction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Đường dẫn đến Database 'auction_db' trên máy
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";     // Mặc định của XAMPP là root
    private static final String PASSWORD = "";     // Mặc định của XAMPP không có mật khẩu

    // Hàm gọi cửa
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("KẾT NỐI DATABASE THẤT BẠI: " + e.getMessage());
            return null;
        }
    }
}
