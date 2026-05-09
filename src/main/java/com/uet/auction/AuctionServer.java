package com.uet.auction;

import com.auction.model.Product;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    // Định nghĩa một "Cổng" để các Client biết đường gõ cửa (Ví dụ: 8888)
    private static final int PORT = 8888;

    // ĐÂY LÀ TRÁI TIM CỦA HỆ THỐNG: Danh sách sản phẩm dùng chung cho mọi người
    public static List<Product> sharedProductList = new ArrayList<>();

    public static List<java.io.ObjectOutputStream> onlineClients = new java.util.ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   HỆ THỐNG MÁY CHỦ ĐẤU GIÁ UET V1.0     ");
        System.out.println("=========================================");
        System.out.println("[SERVER] Đang khởi động và kiểm tra kho dữ liệu...");

        // 1. Máy chủ tự động đọc file ổ cứng tải lên RAM để phục vụ tốc độ cao
        sharedProductList = DataManager.loadProducts();
        System.out.println("[SERVER] Đã tải thành công " + sharedProductList.size() + " sản phẩm từ file products.dat.");

        // 2. Mở cổng kết nối (ServerSocket)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Máy chủ đang mở cửa tại Cổng " + PORT + ".");
            System.out.println("[SERVER] Đang lắng nghe và chờ Client kết nối tới...\n");

            // Vòng lặp vô tận: Luôn luôn thức để đón khách
            while (true) {
                // Lệnh accept() sẽ chặn ứng dụng đứng im tại đây cho đến khi có ai đó kết nối vào
                Socket clientSocket = serverSocket.accept();

                // Lấy địa chỉ IP của máy khách vừa kết nối
                String clientIP = clientSocket.getInetAddress().getHostAddress();

                System.out.println(">>> [KẾT NỐI MỚI] Có một Client vừa truy cập từ IP: " + clientIP);

                // 3. Giao Client này cho một luồng (Thread) riêng xử lý
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();


                // Tạm thời dừng ở đây. Lát nữa sẽ giao Client này cho một "Nhân viên phục vụ" (Thread) xử lý.
            }

        } catch (IOException e) {
            System.err.println("[SERVER LỖI] Không thể mở cổng " + PORT + ". Có thể cổng này đang bị phần mềm khác chiếm dụng.");
            e.printStackTrace();
        }
    }

    // synchronized: Đảm bảo tại 1 thời điểm, chỉ có 1 luồng được phép chạy vào đây
    public static synchronized void updateAndSaveData(List<Product> newProducts) {


        // 1. TÍNH NĂNG ANTI-SNIPING

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Dò xem trong danh sách mới gửi lên, món đồ nào vừa được trả giá
        if (sharedProductList != null) {
            for (Product newP : newProducts) {
                for (Product oldP : sharedProductList) {

                    // So sánh để tìm đúng món đồ đó (dựa vào tên)
                    if (newP.getName().equals(oldP.getName())) {

                        // Nếu giá hiện tại MỚI lớn hơn giá CŨ -> Tức là có người vừa bấm Bid!
                        if (newP.getCurrentPrice() > oldP.getCurrentPrice()) {

                            // Kiểm tra thời gian còn lại
                            java.time.Duration timeLeft = java.time.Duration.between(now, newP.getEndTime());

                            // Nếu thời gian còn lại <= 60 giây và chưa hết hạn (> 0)
                            if (timeLeft.getSeconds() <= 60 && timeLeft.getSeconds() > 0) {

                                // GIA HẠN THÊM 5 PHÚT!
                                newP.setEndTime(newP.getEndTime().plusMinutes(5));
                                System.out.println("[SERVER - ANTI-SNIPING] Bắn tỉa! Đã gia hạn thêm 5 phút cho món: " + newP.getName());
                            }
                        }
                    }
                }
            }
        }
        // ==================================================

        // 2. Cập nhật lên RAM
        sharedProductList = newProducts;

        // 3. Lưu ngay xuống két sắt (ổ cứng) của Server
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream("server_products.dat"))) {
            oos.writeObject(new ArrayList<>(sharedProductList));
            System.out.println("[SERVER] Đã đồng bộ và lưu an toàn xuống ổ cứng (Chống xung đột)!");
        } catch (Exception e) {
            System.err.println("[SERVER LỖI] Lỗi khi lưu két sắt: " + e.getMessage());
        }

        // 4. BROADCAST: Nhắn tin cho toàn bộ Client đang mở app để nhảy số ngay lập tức!
        for (java.io.ObjectOutputStream out : onlineClients) {
            try {
                out.writeObject("UPDATE"); // Gửi mật mã báo hiệu
                out.writeObject(new ArrayList<>(sharedProductList)); // Gửi danh sách mới (đã được cộng giờ nếu có)
                out.flush();
            } catch (Exception e) {
                // Nếu Client nào lỡ tắt app, kệ nó, tí nữa xóa sau
            }
        }
        System.out.println("[SERVER] Đã Broadcast giá và thời gian mới cho tất cả các máy!");
    }
}