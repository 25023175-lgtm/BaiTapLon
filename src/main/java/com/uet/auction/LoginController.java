package com.uet.auction;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 1. Kiểm tra bỏ trống
        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Lỗi nhập liệu", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        // 2. Kéo dữ liệu từ kho lên để đối chiếu
        java.util.List<com.auction.model.User> users = DataManager.loadUsers();
        boolean isSuccess = false;

        for (com.auction.model.User u : users) {
            // Kiểm tra xem tên đăng nhập và mật khẩu có khớp 100% không
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {

                // Đăng nhập đúng -> Cấp "Thẻ căn cước" SessionManager
                SessionManager.currentUser = u;
                isSuccess = true;
                break; // Tìm thấy rồi thì dừng vòng lặp
            }
        }

        // 3. Quyết định cho vào hay đuổi ra
        if (isSuccess) {
            System.out.println("Đăng nhập thành công với vai trò: " + SessionManager.currentUser.getRole());
            SceneManager.switchScene("dashboard-view.fxml", "Hệ thống Đấu giá UET - Trang chủ");
        } else {
            showErrorAlert("Đăng nhập thất bại", "Tài khoản không tồn tại hoặc sai mật khẩu!");
        }
    }

    @FXML
    private void handleSwitchToRegister(ActionEvent event) {
        // chuyển thẳng sang màn hình Đăng ký
        SceneManager.switchScene("register-view.fxml", "Hệ thống Đấu giá - Đăng ký");
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}