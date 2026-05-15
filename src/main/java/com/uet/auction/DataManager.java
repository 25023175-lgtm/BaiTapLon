package com.uet.auction;

import com.auction.factory.UserFactory;
import com.auction.model.Item;
import com.auction.model.Product;
import com.auction.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    // =========================================
    // PHAN 1: QUAN LY NGUOI DUNG (USERS)
    // =========================================
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Dung UserFactory thay vi new User() truc tiep
                User u = UserFactory.createFromDb(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getDouble("balance")
                );
                users.add(u);
            }
        } catch (SQLException e) {
            System.out.println(">> Loi tai User tu DB: " + e.getMessage());
        }
        return users;
    }

    public static void saveUser(User user) {
        String sql = "INSERT INTO users "
                + "(username, password, email, full_name, role) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.executeUpdate();
            System.out.println(">> Da luu User [" + user.getUsername() + "]");

        } catch (SQLException e) {
            System.out.println(">> Loi luu User: " + e.getMessage());
        }
    }

    // =========================================
    // PHAN 2: QUAN LY SAN PHAM (ITEMS/PRODUCTS)
    // =========================================
    public static List<Item> loadProducts() {
        List<Item> products = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email "
                + "FROM products p "
                + "LEFT JOIN users u ON p.seller_username = u.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int sellerId = 0;
                try {
                    sellerId = Integer.parseInt(rs.getString("seller_username"));
                } catch (NumberFormatException ignored) { }

                // Van dung Product (extends Item) de giu tuong thich voi GUI
                Product p = new Product(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("start_price"),
                        rs.getDouble("buy_now_price"),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        sellerId
                );
                p.setId(rs.getInt("id"));
                p.setCurrentPrice(rs.getDouble("current_price"));
                p.setBidCount(rs.getInt("bid_count"));
                p.setSellerName(rs.getString("full_name"));
                p.setSellerEmail(rs.getString("email"));

                // Doc trang thai neu co
                try {
                    String st = rs.getString("status");
                    if (st != null) p.setStatus(st);
                } catch (SQLException ignored) { }

                products.add(p);
            }
        } catch (SQLException e) {
            System.out.println(">> Loi tai san pham tu DB: " + e.getMessage());
        }
        return products;
    }

    public static void saveProduct(Item product) {
        String sql = "INSERT INTO products "
                + "(name, description, start_price, current_price, "
                + "buy_now_price, seller_username, end_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getStartPrice());
            pstmt.setDouble(4, product.getCurrentPrice());
            pstmt.setDouble(5, product.getBuyNowPrice());
            pstmt.setString(6, String.valueOf(product.getSellerId()));
            pstmt.setTimestamp(7,
                    java.sql.Timestamp.valueOf(product.getEndTime()));
            pstmt.setString(8, product.getStatus() != null
                    ? product.getStatus() : "ACTIVE");

            pstmt.executeUpdate();
            System.out.println(">> Da luu san pham [" + product.getName() + "]");
            notifyServer();

        } catch (SQLException e) {
            System.out.println(">> Loi luu san pham: " + e.getMessage());
        }
    }

    public static void deleteProduct(Item product) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getId());
            pstmt.executeUpdate();
            System.out.println(">> Da xoa san pham [" + product.getName() + "]");
            notifyServer();

        } catch (SQLException e) {
            System.out.println(">> Loi xoa san pham: " + e.getMessage());
        }
    }

    public static void updateProduct(Item product) {
        String sql = "UPDATE products "
                + "SET current_price = ?, bid_count = ?, "
                + "end_time = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, product.getCurrentPrice());
            pstmt.setInt(2, product.getBidCount());
            pstmt.setTimestamp(3,
                    java.sql.Timestamp.valueOf(product.getEndTime()));
            pstmt.setString(4,
                    product.getStatus() != null ? product.getStatus() : "ACTIVE");
            pstmt.setInt(5, product.getId());

            pstmt.executeUpdate();
            notifyServer();

        } catch (SQLException e) {
            System.out.println(">> Loi cap nhat san pham: " + e.getMessage());
        }
    }

    public static void saveBid(int productId, double bidPrice) {
        String sql = "INSERT INTO bids (product_id, bid_price) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setDouble(2, bidPrice);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(">> Loi luu bid: " + e.getMessage());
        }
    }

    // =========================================
    // PHAN 3: TU DONG DONG PHIEN & XAC DINH WINNER
    // =========================================

    /**
     * Dong cac phien da het han, tra ve danh sach san pham vua dong.
     * Duoc goi dinh ky boi AuctionManager scheduler.
     */
    public static List<Item> closeExpiredAuctions() {
        List<Item> closed = new ArrayList<>();
        String sql = "UPDATE products "
                + "SET status = 'ENDED' "
                + "WHERE status = 'ACTIVE' AND end_time <= NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Lay danh sach san pham vua bi dong
                String selectSql = "SELECT p.*, u.full_name, u.email "
                        + "FROM products p "
                        + "LEFT JOIN users u ON p.seller_username = u.id "
                        + "WHERE p.status = 'ENDED' "
                        + "AND p.end_time >= NOW() - INTERVAL 1 MINUTE";

                try (PreparedStatement ps2 = conn.prepareStatement(selectSql);
                     ResultSet rs = ps2.executeQuery()) {
                    while (rs.next()) {
                        Product p = new Product(
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getDouble("start_price"),
                                rs.getDouble("buy_now_price"),
                                rs.getTimestamp("end_time").toLocalDateTime(),
                                0
                        );
                        p.setId(rs.getInt("id"));
                        p.setCurrentPrice(rs.getDouble("current_price"));
                        p.setStatus("ENDED");
                        closed.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(">> Loi dong phien: " + e.getMessage());
        }
        return closed;
    }

    /**
     * Lay ten nguoi thang cuoc (bidder co gia cao nhat trong san pham).
     */
    public static String getWinnerForItem(int productId) {
        String sql = "SELECT u.full_name "
                + "FROM bids b "
                + "JOIN users u ON b.bidder_id = u.id "
                + "WHERE b.product_id = ? "
                + "ORDER BY b.bid_price DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("full_name");

        } catch (SQLException e) {
            // Fallback: lay tu gia cao nhat
            return getWinnerFallback(productId);
        }
        return "Chua co nguoi dat gia";
    }

    private static String getWinnerFallback(int productId) {
        // Neu bang bids chua co bidder_id, lay bang gia cao nhat
        String sql = "SELECT MAX(bid_price) as max_price FROM bids "
                + "WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Nguoi dat gia cao nhat: "
                        + String.format("%,.0f VND",
                        rs.getDouble("max_price")).replace(",", ".");
            }
        } catch (SQLException ignored) { }
        return "Khong ro";
    }

    // =========================================
    // PHAN 4: GIAO TIEP MANG (REAL-TIME)
    // =========================================
    public static void startRealtimeListener(DashboardController controller) {
        new Thread(() -> {
            try {
                java.net.Socket socket =
                        new java.net.Socket("localhost", 8888);
                java.io.ObjectOutputStream out =
                        new java.io.ObjectOutputStream(socket.getOutputStream());
                out.flush();
                java.io.ObjectInputStream in =
                        new java.io.ObjectInputStream(socket.getInputStream());

                out.writeObject("LISTEN");
                out.flush();

                while (true) {
                    String signal = (String) in.readObject();
                    if ("REFRESH_DATA".equals(signal)) {
                        List<Item> freshList = loadProducts();
                        javafx.application.Platform.runLater(() ->
                                controller.refreshTable(freshList));
                    }
                }
            } catch (Exception e) {
                System.err.println("[CLIENT] Mat ket noi voi Server.");
            }
        }, "RealtimeListener").start();
    }

    public static void notifyServer() {
        new Thread(() -> {
            try (java.net.Socket socket =
                         new java.net.Socket("localhost", 8888);
                 java.io.ObjectOutputStream out =
                         new java.io.ObjectOutputStream(
                                 socket.getOutputStream())) {
                out.writeObject("CLIENT_UPDATE");
                out.flush();
            } catch (Exception e) {
                System.out.println(">> Server chua bat, bo qua broadcast.");
            }
        }, "NotifyServer").start();
    }
}