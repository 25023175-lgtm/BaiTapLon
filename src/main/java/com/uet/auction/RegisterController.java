package com.uet.auction;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Bidder", "Seller");
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        System.out.println("Đăng ký tài khoản: " + regUsernameField.getText());
        System.out.println("Vai trò đã chọn: " + roleComboBox.getValue());
        // Code xử lý lưu người dùng mới sẽ nằm ở đây
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        // quay ngược lại màn hình Đăng nhập
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá - Đăng nhập");
    }
}