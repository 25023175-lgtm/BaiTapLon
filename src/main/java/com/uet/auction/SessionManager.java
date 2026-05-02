package com.uet.auction;

import com.auction.model.User;

public class SessionManager {
    // 1. Khai báo 1 biến static private để giữ "bản sao" duy nhất
    private static SessionManager instance;

    // Biến lưu người dùng hiện tại
    private User currentUser;

    // 2. [QUAN TRỌNG NHẤT] Private Constructor: Cấm không cho bất cứ ai dùng lệnh 'new SessionManager()'
    private SessionManager() {
    }

    // 3. Hàm cổng để lấy bản sao duy nhất ra dùng (Lazy Initialization)
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
            System.out.println("[Singleton] Đã khởi tạo phiên làm việc duy nhất!");
        }
        return instance;
    }

    // Các hàm thao tác với user
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }
}