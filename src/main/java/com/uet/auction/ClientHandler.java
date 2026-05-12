package com.uet.auction;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// implements Runnable giúp class này có khả năng chạy song song (Đa luồng)
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 1. Mở ống dẫn
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 2. Lắng nghe yêu cầu từ Client
            String command = (String) in.readObject();

            if ("LISTEN".equals(command)) {
                // Client này muốn cắm máy ở đây để nhận thông báo Real-time
                AuctionServer.onlineClients.add(out);
                System.out.println("[Server] Một Client vừa tham gia mạng lưới Real-time.");

                // Treo máy đứng chờ. Nếu Client này gửi mật mã báo hiệu nó vừa thao tác DB xong...
                while (true) {
                    String msg = (String) in.readObject();
                    if ("CLIENT_UPDATE".equals(msg)) {
                        System.out.println("[Server] Nhận được báo cáo thay đổi từ Client!");
                        // ...thì lập tức gọi Server phát loa cho tất cả các máy khác!
                        AuctionServer.broadcastUpdate();
                    }
                }
            }
            else if ("CLIENT_UPDATE".equals(command)) {
                // Nếu Client chỉ kết nối chớp nhoáng 1 giây để báo cập nhật rồi tắt
                AuctionServer.broadcastUpdate();
                socket.close();
            }

        } catch (Exception e) {
            // Lỗi này xảy ra khi Client tự động tắt App (đứt kết nối)
            System.out.println(">>> [KẾT THÚC] Một Client đã rời mạng lưới.");
        } finally {
            // Dọn dẹp: Xóa ống dẫn của Client này khỏi danh bạ để Server khỏi gửi nhầm
            if (out != null) {
                AuctionServer.onlineClients.remove(out);
            }
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}