package com.uet.auction;

import com.auction.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    // =========================================
    // PHẦN 1: QUẢN LÝ NGƯỜI DÙNG (USERS)
    // =========================================
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User u = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("role")
                );
                u.setId(rs.getInt("id"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.out.println(">> Lỗi khi tải danh sách User từ DB: " + e.getMessage());
        }
        return users;
    }

    public static void saveUser(User user) {
        String sql = "INSERT INTO users (username, password, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());

            pstmt.executeUpdate();
            System.out.println(">> Đã lưu thành công User [" + user.getUsername() + "] vào MySQL!");

        } catch (SQLException e) {
            System.out.println(">> Lỗi khi lưu User vào DB: " + e.getMessage());
        }
    }

    // =========================================
    // PHẦN 2: QUẢN LÝ SẢN PHẨM (PRODUCTS)
    // =========================================
    public static java.util.List<com.auction.model.Product> loadProducts() {
        java.util.List<com.auction.model.Product> products = new java.util.ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                com.auction.model.Product p = new com.auction.model.Product(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("start_price"),
                        rs.getDouble("buy_now_price"),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        0    // sellerId tạm thời
                );

                p.setId(rs.getInt("id"));
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setBidCount(rs.getInt("bid_count"));

                products.add(p);
            }
        } catch (SQLException e) {
            System.out.println(">> Lỗi khi tải danh sách Sản phẩm từ DB: " + e.getMessage());
        }
        return products;
    }

    public static void saveProduct(com.auction.model.Product product) {
        String sql = "INSERT INTO products (name, description, start_price, current_price, buy_now_price, seller_username, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getStartPrice());
            pstmt.setDouble(4, product.getCurrentPrice());
            pstmt.setDouble(5, product.getBuyNowPrice());
            pstmt.setString(6, String.valueOf(product.getSellerId()));
            pstmt.setTimestamp(7, java.sql.Timestamp.valueOf(product.getEndTime()));

            pstmt.executeUpdate();
            System.out.println(">> Đã lưu thành công Sản phẩm [" + product.getName() + "] vào MySQL!");

            // [MỚI] BÁO CHO SERVER BIẾT VỪA CÓ HÀNG MỚI
            notifyServer();

        } catch (SQLException e) {
            System.out.println(">> Lỗi khi lưu Sản phẩm vào DB: " + e.getMessage());
        }
    }

    public static void deleteProduct(com.auction.model.Product product) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getId());
            pstmt.executeUpdate();
            System.out.println(">> Đã xóa thành công Sản phẩm [" + product.getName() + "] khỏi Database!");

            // [MỚI] BÁO CHO SERVER BIẾT VỪA XÓA HÀNG
            notifyServer();

        } catch (SQLException e) {
            System.out.println(">> Lỗi khi xóa Sản phẩm: " + e.getMessage());
        }
    }

    public static void updateProduct(com.auction.model.Product product) {
        String sql = "UPDATE products SET current_price = ?, bid_count = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, product.getCurrentPrice());
            pstmt.setInt(2, product.getBidCount());
            pstmt.setInt(3, product.getId());

            pstmt.executeUpdate();
            System.out.println(">> Đã cập nhật giá mới cho Sản phẩm ID [" + product.getId() + "]");

            // [MỚI] BÁO CHO SERVER BIẾT VỪA CÓ BIẾN ĐỘNG GIÁ
            notifyServer();

        } catch (SQLException e) {
            System.out.println(">> Lỗi khi cập nhật giá Sản phẩm: " + e.getMessage());
        }
    }

    public static void saveBid(int productId, double bidPrice) {
        String sql = "INSERT INTO bids (product_id, bid_price) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setDouble(2, bidPrice);
            pstmt.executeUpdate();
            System.out.println(">> Đã ghi nhận lịch sử: Sản phẩm ID " + productId + " có mức giá mới " + bidPrice);

        } catch (SQLException e) {
            System.out.println(">> Lỗi khi lưu lịch sử Bid: " + e.getMessage());
        }
    }


    // =========================================
    // PHẦN 3: GIAO TIẾP MẠNG (REAL-TIME SERVER)
    // =========================================

    /**
     * Hàm này cắm một cái ăng-ten để nghe lệnh từ Server.
     * Hễ Server hô "REFRESH_DATA" là tự động kéo MySQL về nạp lên bảng.
     */
    public static void startRealtimeListener(com.uet.auction.DashboardController controller) {
        new Thread(() -> {
            try {
                java.net.Socket socket = new java.net.Socket("localhost", 8888);
                java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
                out.flush();
                java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream());

                // Xin gia nhập mạng lưới nhận thông báo
                out.writeObject("LISTEN");
                out.flush();

                while (true) {
                    String signal = (String) in.readObject();

                    if ("REFRESH_DATA".equals(signal)) {
                        // 1. Tự động lấy danh sách mới nhất từ MySQL
                        List<com.auction.model.Product> freshList = loadProducts();

                        // 2. Ép giao diện JavaFX cập nhật lại cái Bảng
                        javafx.application.Platform.runLater(() -> {
                            controller.refreshTable(freshList);
                            System.out.println("[CLIENT] Đã tải lại dữ liệu MySQL theo lệnh của Server!");
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("[CLIENT] Tạm thời mất kết nối với Server.");
            }
        }).start();
    }

    /**
     * Hàm này giống như cái chuông báo.
     * Cứ thêm/xóa/sửa Database xong là gọi hàm này để báo Server
     */
    public static void notifyServer() {
        new Thread(() -> {
            try (java.net.Socket socket = new java.net.Socket("localhost", 8888);
                 java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream())) {

                // Báo cáo Server: "Có dữ liệu thay đổi!"
                out.writeObject("CLIENT_UPDATE");
                out.flush();

            } catch (Exception e) {
                System.out.println(">> [BỘ ĐÀM] Chưa bật Server nên không phát được thông báo.");
            }
        }).start();
    }
}