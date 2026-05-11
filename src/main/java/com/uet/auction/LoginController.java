package com.uet.auction;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class LoginController {

    @FXML
    private TextField usernameField;

    // Đã sửa tên biến để khớp với FXML mới
    @FXML
    private PasswordField hiddenPasswordField;

    // Khai báo thêm 2 biến mới cho tính năng "con mắt"
    @FXML
    private TextField visiblePasswordField;

    @FXML
    private ToggleButton togglePasswordBtn;

    // HÀM KHỞI TẠO: Chạy ngay khi màn hình vừa mở lên
    @FXML
    public void initialize() {
        // 1. Đồng bộ 2 ô text
        visiblePasswordField.textProperty().bindBidirectional(hiddenPasswordField.textProperty());

        // 2. Bắt sự kiện click vào con mắt
        togglePasswordBtn.setOnAction(event -> {
            if (togglePasswordBtn.isSelected()) {
                // Đang bấm xuống -> Mở mắt -> Hiện chữ, Ẩn dấu chấm
                visiblePasswordField.setVisible(true);
                hiddenPasswordField.setVisible(false);
                togglePasswordBtn.setText("🙈"); // Icon nhắm mắt
            } else {
                // Nhả ra -> Nhắm mắt -> Ẩn chữ, Hiện dấu chấm
                visiblePasswordField.setVisible(false);
                hiddenPasswordField.setVisible(true);
                togglePasswordBtn.setText("👁");  // Icon mở mắt
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();


        String password = hiddenPasswordField.getText();

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
                SessionManager.getInstance().setCurrentUser(u);
                isSuccess = true;
                break; // Tìm thấy rồi thì dừng vòng lặp
            }
        }

        // 3. Quyết định cho vào hay đuổi ra
        if (isSuccess) {
            System.out.println("Đăng nhập thành công với vai trò: " + SessionManager.getInstance().getCurrentUser().getRole());
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