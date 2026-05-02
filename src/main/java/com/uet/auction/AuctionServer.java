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
    // Dòng này thêm ngay dưới dòng khai báo sharedProductList
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
                // ...

                // Tạm thời dừng ở đây. Lát nữa chúng ta sẽ giao Client này cho một "Nhân viên phục vụ" (Thread) xử lý.
            }

        } catch (IOException e) {
            System.err.println("[SERVER LỖI] Không thể mở cổng " + PORT + ". Có thể cổng này đang bị phần mềm khác chiếm dụng.");
            e.printStackTrace();
        }
    }

    // 1. Hàm này dùng từ khóa synchronized: Đảm bảo tại 1 thời điểm, chỉ có 1 luồng được phép chạy vào đây
    public static synchronized void updateAndSaveData(List<Product> newProducts) {
        // Cập nhật lên RAM
        sharedProductList = newProducts;

        // Lưu ngay xuống két sắt (ổ cứng) của Server
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream("server_products.dat"))) {
            oos.writeObject(new ArrayList<>(sharedProductList));
            System.out.println("[SERVER] Đã đồng bộ và lưu an toàn xuống ổ cứng (Chống xung đột)!");
        } catch (Exception e) {
            System.err.println("[SERVER LỖI] Lỗi khi lưu két sắt: " + e.getMessage());
        }

        // 2. BROADCAST: Nhắn tin cho toàn bộ Client đang mở app để họ nhảy số ngay lập tức!
        for (java.io.ObjectOutputStream out : onlineClients) {
            try {
                out.writeObject("UPDATE"); // Gửi mật mã báo hiệu
                out.writeObject(new ArrayList<>(sharedProductList)); // Gửi danh sách mới
                out.flush();
            } catch (Exception e) {
                // Nếu Client nào lỡ tắt app, kệ nó, tí nữa xóa sau
            }
        }
        System.out.println("[SERVER] Đã Broadcast giá mới cho tất cả các máy!");
    }

}
