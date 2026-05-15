package com.uet.auction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    // Định nghĩa một "Cổng" để các Client biết đường gõ cửa
    private static final int PORT = 8888;

    // Chỉ lưu danh sách các "ống dẫn" đến các Client đang online để gửi thông báo
    public static List<ObjectOutputStream> onlineClients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   HỆ THỐNG MÁY CHỦ ĐẤU GIÁ UET V2.0     ");
        System.out.println("       (TÍCH HỢP MYSQL DATABASE)         ");
        System.out.println("=========================================");

        // Mở cổng kết nối (ServerSocket)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Máy chủ đang mở cửa tại Cổng " + PORT + ".");
            System.out.println("[SERVER] Đang lắng nghe và chờ Client kết nối tới...\n");

            // Vòng lặp vô tận: Luôn luôn thức để đón khách
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println(">>> [KẾT NỐI MỚI] Có một Client vừa truy cập từ IP: " + clientIP);

                // Giao Client này cho một luồng (Thread) riêng xử lý
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("[SERVER LỖI] Không thể mở cổng " + PORT + ". Có thể cổng này đang bị phần mềm khác chiếm dụng.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // HÀM QUAN TRỌNG: PHÁT TÍN HIỆU LÀM MỚI (BROADCAST)
    // =========================================================
    public static synchronized void broadcastUpdate() {
        // Tạo danh sách tạm để hứng những Client đã tắt app (tránh lỗi)
        List<ObjectOutputStream> disconnected = new ArrayList<>();

        for (ObjectOutputStream out : onlineClients) {
            try {
                // Chỉ gửi một mật mã chuỗi ngắn gọn thay vì gửi cả List nặng nề
                out.writeObject("REFRESH_DATA");
                out.flush();
            } catch (Exception e) {
                // Nếu lỗi tức là Client này đã ngắt kết nối
                disconnected.add(out);
            }
        }

        // Dọn dẹp các Client đã rời đi
        onlineClients.removeAll(disconnected);
        System.out.println("[SERVER] Đã Broadcast tín hiệu REFRESH cho " + onlineClients.size() + " máy đang online!");
    }
}