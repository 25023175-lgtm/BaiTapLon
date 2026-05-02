package com.uet.auction;

import com.auction.model.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BidController {

    @FXML private Label productNameLabel;
    @FXML private Label currentPriceLabel;
    @FXML private TextField bidAmountField;

    private Product currentProduct;

    // 1. Hàm này dùng để NHẬN dữ liệu từ màn hình Dashboard ném sang
    public void setProductData(Product product) {
        this.currentProduct = product;
        productNameLabel.setText("Đang đấu giá: " + product.getName());

        // Hiển thị giá hiện tại đẹp mắt
        String formattedPrice = String.format("%,.0f", product.getCurrentPrice()).replace(",", ".");
        currentPriceLabel.setText("Giá hiện tại: " + formattedPrice + " VNĐ");
    }

    // 2. Xử lý khi bấm nút "Ra giá"
    @FXML
    private void handlePlaceBid(ActionEvent event) {
        try {
            // Lấy số tiền người dùng nhập
            double bidAmount = Double.parseDouble(bidAmountField.getText());

            // Luật đấu giá: Tiền đưa ra phải lớn hơn giá hiện tại
            if (bidAmount <= currentProduct.getCurrentPrice()) {
                showError("Lỗi ra giá", "Số tiền đấu giá phải LỚN HƠN giá hiện tại!");
                return;
            }

            // Cập nhật giá mới vào sản phẩm
            currentProduct.setCurrentPrice(bidAmount);

            // --- THÊM ĐOẠN NÀY ĐỂ ĐỒNG BỘ REALTIME ---
            try {
                // 1. Tải danh sách mới nhất từ Server về (để không vô tình ghi đè mất món đồ người khác vừa thêm)
                java.util.List<com.auction.model.Product> allProducts = DataManager.loadProducts();

                // 2. Tìm đúng món đồ này trong kho và chốt giá mới
                for (com.auction.model.Product p : allProducts) {
                    // Đối chiếu bằng tên sản phẩm để tìm đúng món đồ cần sửa giá
                    if (p.getName().equals(currentProduct.getName())) {
                        p.setCurrentPrice(bidAmount);
                        break;
                    }
                }

                // 3. Gửi danh sách đã chốt giá lên Server để Broadcast cho toàn mạng
                DataManager.saveProducts(allProducts);
                System.out.println("[CLIENT] Đã gửi giá đấu mới lên Server!");

            } catch (Exception e) {
                showError("Lỗi mạng", "Không thể đồng bộ giá lên máy chủ!");
                return;
            }

            // Báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Bạn đã ra giá thành công!");
            alert.showAndWait();

            // Tự động đóng cửa sổ Pop-up
            Stage stage = (Stage) bidAmountField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("Lỗi định dạng", "Vui lòng chỉ nhập số (Ví dụ: 25000000)");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}