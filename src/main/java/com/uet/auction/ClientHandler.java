package com.uet.auction;

import com.auction.model.Product;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

// implements Runnable giúp class này có khả năng chạy song song (Đa luồng)
public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 1. Lắng nghe xem Client hét mật mã gì?
            String command = (String) in.readObject();

            if (command.equals("GET")) {
                // Nếu xin dữ liệu -> Lấy ở kho ném trả về
                out.writeObject(AuctionServer.sharedProductList);
                out.flush();
                System.out.println("[Server] Đã gửi danh sách cho Client.");

            } else if (command.equals("SAVE")) {
                // Nếu gửi hàng lưu -> Nhận lấy và cất vào kho
                List<com.auction.model.Product> updatedList = (List<com.auction.model.Product>) in.readObject();
                AuctionServer.updateAndSaveData(updatedList);
                System.out.println("[Server] Đã cập nhật kho hàng thành công. Có " + updatedList.size() + " món.");

            } else if (command.equals("LISTEN")) {
                // 1. Ghi tên vào Danh bạ Nhóm chat
                AuctionServer.onlineClients.add(out);
                System.out.println("[Server] Một Client vừa tham gia nhóm nhận thông báo Realtime.");

                // 2. Gửi luôn dữ liệu mới nhất
                out.writeObject("UPDATE");
                out.writeObject(AuctionServer.sharedProductList);
                out.flush();

                // 3. Treo máy đứng chờ ở đây (Vòng lặp vô tận)
                while (true) {
                    in.readObject();
                }
            } // <-- Dấu ngoặc này chính là cái bạn bị thiếu để đóng nhánh LISTEN!

            // Xong việc thì cúp máy (Chỉ áp dụng cho GET và SAVE, nhánh LISTEN đã bị treo ở trên)
            socket.close();

        } catch (Exception e) { // <-- Dấu ngoặc trước chữ catch này để đóng khối try!
            System.out.println("[LỖI SERVER] Giao tiếp với Client bị gián đoạn.");
        }
    }
}