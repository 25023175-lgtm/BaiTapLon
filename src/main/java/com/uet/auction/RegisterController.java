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
        // Dùng trim() để cắt khoảng trắng thừa ở 2 đầu (trừ mật khẩu)
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleComboBox.getValue();

        // 1. KIỂM TRA LỖI NHẬP LIỆU
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            showErrorAlert("Lỗi nhập liệu", "Vui lòng điền đầy đủ tất cả thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorAlert("Lỗi mật khẩu", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // 2. KIỂM TRA ĐIỀU KIỆN 1: TÊN ĐĂNG NHẬP
        if (username.length() < 5) {
            showErrorAlert("Tên đăng nhập không hợp lệ",
                    "Tên đăng nhập phải có ít nhất 5 ký tự.");
            return;
        }

        // 3. KIỂM TRA ĐIỀU KIỆN 2: MẬT KHẨU
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s]).{8,12}$")) {
            showErrorAlert("Mật khẩu không an toàn",
                    "Mật khẩu phải từ 8-12 kí tự, bao gồm chữ cái thường, CHỮ HOA, chữ số và ít nhất 1 kí tự đặc biệt (không chứa khoảng trắng).");
            return;
        }

        // 4. KIỂM TRA ĐIỀU KIỆN 3: HỌ VÀ TÊN VIẾT HOA
        if (!isValidFullName(fullName)) {
            showErrorAlert("Họ và tên không hợp lệ",
                    "Họ và tên phải được viết hoa chữ cái đầu tiên của mỗi từ.\nVí dụ: Đặng Duy Hưng");
            return;
        }

        // 5. KIỂM TRA ĐIỀU KIỆN 4: EMAIL HOẶC SỐ ĐIỆN THOẠI (Đã gỡ bỏ bắt buộc @gmail.com)
        boolean isEmail = email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        boolean isPhone = email.matches("^0\\d{9}$");

        if (!isEmail && !isPhone) {
            showErrorAlert("Thông tin liên hệ không hợp lệ",
                    "Vui lòng nhập đúng định dạng Email (ví dụ: a@gmail.com)\nHoặc Số điện thoại (10 chữ số, bắt đầu bằng số 0).");
            return;
        }

        // =========================================================
        // 6. KIỂM TRA TRÙNG LẶP
        // Đọc danh sách user từ file lên để dò
        java.util.List<User> existingUsers = DataManager.loadUsers();

        if (existingUsers != null) {
            for (User u : existingUsers) {
                // Kiểm tra trùng Họ Tên VÀ trùng Vai trò
                if (u.getFullName().equalsIgnoreCase(fullName) && u.getRole().equalsIgnoreCase(role)) {
                    showErrorAlert("Người dùng đã tồn tại",
                            "Hệ thống ghi nhận bạn đã có một tài khoản với vai trò " + role + " rồi!");
                    return;
                }

                // Kiểm tra trùng Email/SĐT VÀ trùng Vai trò
                if (u.getEmail().equalsIgnoreCase(email) && u.getRole().equalsIgnoreCase(role)) {
                    showErrorAlert("Thông tin liên hệ đã được sử dụng",
                            "Email/Số điện thoại này đã được dùng cho một tài khoản " + role + " khác!");
                    return;
                }
            }
        }
        // =========================================================

        // 7. VƯỢT QUA KIỂM TRA -> TẠO USER MỚI
        User newUser = new User(username, password, email, fullName, role);

        System.out.println("Đăng ký tài khoản: " + newUser.getUsername());
        System.out.println("Họ tên: " + newUser.getFullName());
        System.out.println("Vai trò đã chọn: " + role);

        // Gọi DataManager để lưu người dùng vào file
        DataManager.saveUser(newUser);

        // Hiển thị một bảng thông báo thành công
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

    // Hàm hỗ trợ kiểm tra viết hoa chữ cái đầu
    private boolean isValidFullName(String name) {
        if (name == null || name.trim().isEmpty()) return false;

        // Cắt tên thành các từ cách nhau bởi dấu cách
        String[] words = name.trim().split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) continue;
            // Kiểm tra chữ cái đầu tiên của mỗi từ xem có phải in hoa không
            if (!Character.isUpperCase(word.charAt(0))) {
                return false;
            }
        }
        return true;
    }

    // Hàm hiển thị bảng báo lỗi
    private void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}