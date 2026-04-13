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

        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Lỗi nhập liệu", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        System.out.println("Đăng nhập: " + username);
        // Sau này khi đăng nhập thành công, bạn sẽ gọi:
        // SceneManager.switchScene("dashboard-view.fxml", "Bảng điều khiển");
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