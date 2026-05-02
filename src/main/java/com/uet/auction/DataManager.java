package com.uet.auction;

import com.auction.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.auction.model.Product;

public class DataManager {
    // Tên file để lưu trữ dữ liệu người dùng
    private static final String USER_FILE = "users.dat";

    // 1. Hàm đọc danh sách người dùng từ file lên
    public static List<User> loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return new ArrayList<>(); // Nếu lần đầu chạy chưa có file, trả về danh sách rỗng
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<User>) ois.readObject();
        } catch (Exception e) {
            System.out.println("Lỗi khi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 2. Hàm cất một người dùng mới vào file
    public static void saveUser(User newUser) {
        // Lấy danh sách cũ
        List<User> users = loadUsers();

        // Tự động cấp mã ID (Người đầu tiên là 1, người sau lấy ID cuối + 1)
        int newId = users.isEmpty() ? 1 : users.get(users.size() - 1).getId() + 1;
        newUser.setId(newId);

        // Nhét người mới vào danh sách
        users.add(newUser);

        // Đóng gói và ghi đè toàn bộ danh sách xuống file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
            System.out.println("Đã lưu thành công tài khoản vào ổ cứng!");
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    private static final String PRODUCT_FILE = "products.dat";

    // 1. Tải danh sách sản phẩm từ file
    // 1. Tải danh sách sản phẩm TỪ MÁY CHỦ (Thay vì đọc file cứng)
    public static List<com.auction.model.Product> loadProducts() {
        try (java.net.Socket socket = new java.net.Socket("localhost", 8888)) {

            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
            out.flush();
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream());

            // 1. THÊM ĐOẠN NÀY: Hét lên mật mã cho Server biết mình muốn gì!
            out.writeObject("GET");
            out.flush(); // Bắt buộc phải có flush để đẩy chữ GET bay qua mạng

            System.out.println("[CLIENT] Đã gửi yêu cầu GET, đang chờ Server trả lời...");

            // 2. Nhận kết quả Server ném về
            List<com.auction.model.Product> result = (List<com.auction.model.Product>) in.readObject();
            System.out.println("[CLIENT] Nhận dữ liệu thành công!");
            return result;

        } catch (Exception e) {
            System.err.println("[CLIENT LỖI] Không thể kết nối tới Server. Đang dùng danh sách trống.");
            return new java.util.ArrayList<>();
        }
    }



    // 2. Gửi lệnh "SAVE" để đẩy dữ liệu mới lên Server
    public static void saveProducts(List<com.auction.model.Product> products) {
        try (java.net.Socket socket = new java.net.Socket("localhost", 8888)) {
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
            out.flush();
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream());

            // Hét lên mật mã: HÃY LƯU CÁI NÀY VÀO KHO!
            out.writeObject("SAVE");
            out.flush();

            // Gửi cục hàng (danh sách) qua mạng
            out.writeObject(new ArrayList<>(products));
            out.flush();

            System.out.println("[CLIENT] Đã gửi bản cập nhật lên Trạm chỉ huy (Server)!");
        } catch (Exception e) {
            System.err.println("[CLIENT LỖI] Không thể gửi dữ liệu lên Server.");
        }
    }

    // Hàm này mở một luồng chạy ngầm liên tục nghe ngóng Server
    public static void startRealtimeListener(com.uet.auction.DashboardController controller) {
        new Thread(() -> {
            try {
                java.net.Socket socket = new java.net.Socket("localhost", 8888);
                java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
                out.flush();
                java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream());

                // Gửi mật mã xin gia nhập nhóm chat Realtime
                out.writeObject("LISTEN");
                out.flush();

                while (true) {
                    String signal = (String) in.readObject();
                    if (signal.equals("UPDATE")) {
                        List<com.auction.model.Product> newList = (List<com.auction.model.Product>) in.readObject();

                        // CỰC KỲ QUAN TRỌNG TRONG JAVAFX:
                        // Khi 1 luồng ngầm muốn thay đổi giao diện (UI), phải xin phép thông qua Platform.runLater
                        javafx.application.Platform.runLater(() -> {
                            controller.refreshTable(newList);
                            System.out.println("[CLIENT] Giao diện vừa tự động giật số Realtime!");
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("[CLIENT] Đã ngắt kết nối Realtime.");
            }
        }).start(); // Bấm nút Start cho luồng ngầm chạy
    }
}

