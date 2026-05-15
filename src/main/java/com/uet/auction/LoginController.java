package com.uet.auction;

import com.auction.common.AuthenticationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField hiddenPasswordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ToggleButton togglePasswordBtn;

    @FXML
    public void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(hiddenPasswordField.textProperty());

        togglePasswordBtn.setOnAction(event -> {
            if (togglePasswordBtn.isSelected()) {
                visiblePasswordField.setVisible(true);
                hiddenPasswordField.setVisible(false);
                togglePasswordBtn.setText("🙈");
            } else {
                visiblePasswordField.setVisible(false);
                hiddenPasswordField.setVisible(true);
                togglePasswordBtn.setText("👁");
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = hiddenPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Lỗi nhập liệu", "Vui lòng điền đầy đủ\ntên đăng nhập và mật khẩu!");
            return;
        }

        java.util.List<com.auction.model.User> users = DataManager.loadUsers();
        boolean isSuccess = false;

        for (com.auction.model.User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                SessionManager.getInstance().setCurrentUser(u);
                isSuccess = true;
                break;
            }
        }

        try {
            if (!isSuccess) {
                throw new AuthenticationException(
                        "Tai khoan khong ton tai hoac mat khau sai!");
            }
            SceneManager.switchScene("dashboard-view.fxml",
                    "He thong Dau gia UET - Trang chu");
        } catch (AuthenticationException e) {
            showErrorAlert("Dang nhap that bai", e.getMessage());
        }
    }

    @FXML
    private void handleSwitchToRegister(ActionEvent event) {
        SceneManager.switchScene("register-view.fxml", "Hệ thống Đấu giá - Đăng ký");
    }

    // ── Alert lỗi có style xanh lá / đỏ ──────────────────────────────
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Chọn màu header theo loại lỗi
        boolean isLoginFail = title.toLowerCase().contains("thất bại")
                || title.toLowerCase().contains("mật khẩu");
        String headerColor = isLoginFail ? "#DC2626" : "#D97706"; // đỏ hoặc cam
        String iconText    = isLoginFail ? "🔐" : "⚠️";

        // ── Header ────────────────────────────────────────────────────
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(8);
        header.setStyle(
                "-fx-background-color: " + headerColor + ";" +
                        "-fx-padding: 22 24 18 24;" +
                        "-fx-alignment: CENTER;"
        );
        javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(iconText);
        iconLabel.setStyle("-fx-font-size: 30px;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;"
        );
        header.getChildren().addAll(iconLabel, titleLabel);

        // ── Body ──────────────────────────────────────────────────────
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(0);
        body.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 18 24 12 24;" +
                        "-fx-alignment: CENTER;"
        );
        javafx.scene.control.Label msgLabel = new javafx.scene.control.Label(content);
        msgLabel.setStyle(
                "-fx-font-size: 13.5px; -fx-text-fill: #374151;" +
                        "-fx-text-alignment: center; -fx-line-spacing: 3;"
        );
        msgLabel.setWrapText(true);
        body.getChildren().add(msgLabel);

        // ── Assemble ──────────────────────────────────────────────────
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(320);
        root.getChildren().addAll(header, body);

        alert.getDialogPane().setContent(root);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;");

        ButtonType btnOk = new ButtonType("Đã hiểu",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnOk);

        alert.setOnShown(ev -> {
            javafx.scene.Node btn = alert.getDialogPane().lookupButton(btnOk);
            if (btn != null) btn.setStyle(
                    "-fx-background-color: " + headerColor + "; -fx-text-fill: white;" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 9 24;"
            );
        });

        alert.showAndWait();
    }
}