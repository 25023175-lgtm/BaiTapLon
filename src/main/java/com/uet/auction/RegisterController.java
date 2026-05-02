package com.uet.auction;

import com.auction.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField regUsernameField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML private TextField emailField;
    @FXML private TextField fullNameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Bidder", "Seller");
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        String fullName = fullNameField.getText();
        String role = roleComboBox.getValue();

        // 2. Kiểm tra lỗi nhập liệu
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            showErrorAlert("Lỗi nhập liệu", "Vui lòng điền đầy đủ tất cả thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorAlert("Lỗi mật khẩu", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // 3. Tạo User mới theo chuẩn của Thành viên A
        User newUser = new User(username, password, email, fullName, role);

        System.out.println("Đăng ký tài khoản: " + newUser.getUsername());
        System.out.println("Họ tên: " + newUser.getFullName());
        System.out.println("Vai trò đã chọn: " + role);

        // Code xử lý lưu người dùng mới sẽ nằm ở đây sau này
        // Gọi DataManager để lưu người dùng vào file
        DataManager.saveUser(newUser);

        // Hiển thị một bảng thông báo thành công cho rực rỡ
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Thành công");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Chúc mừng bạn đã đăng ký tài khoản thành công!");
        successAlert.showAndWait();

        // Đăng ký thành công thì chuyển về màn Login
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá - Đăng nhập");
    }
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        // quay ngược lại màn hình Đăng nhập
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá - Đăng nhập");
    }

    // Hàm hiển thị bảng báo lỗi
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}