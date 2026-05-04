package com.uet.auction;

import com.auction.model.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.auction.common.AuctionClosedException;
import com.auction.common.InvalidBidException;

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
     void handlePlaceBid(ActionEvent event) {
        try {
            // Lấy số tiền người dùng nhập
            double bidAmount = Double.parseDouble(bidAmountField.getText());

            // Luật đấu giá: Tiền đưa ra phải lớn hơn giá hiện tại
            // 1. BẮT LỖI 1: Nếu phiên đấu giá không còn chữ "ACTIVE" (Đang đấu giá)
            if (currentProduct.getStatus() != null && !currentProduct.getStatus().equals("ACTIVE")) {
                throw new AuctionClosedException("Món đồ này đã chốt đơn, không thể ra giá thêm!");
            }

            // 2. BẮT LỖI 2: Nếu tiền ra giá nhỏ hơn hoặc bằng giá hiện tại
            if (bidAmount <= currentProduct.getCurrentPrice()) {
                throw new InvalidBidException("Số tiền đấu giá phải cao hơn giá hiện tại!");
            }

            // Cập nhật giá mới vào sản phẩm
            currentProduct.setCurrentPrice(bidAmount);


            try {
                // 1. Tải danh sách mới nhất từ Server về
                java.util.List<com.auction.model.Product> allProducts = DataManager.loadProducts();

                // 2. Tìm đúng món đồ này trong kho và chốt giá mới
                for (com.auction.model.Product p : allProducts) {
                    if (p.getName().equals(currentProduct.getName())) {
                        p.setCurrentPrice(bidAmount);
                        break;
                    }
                }

                // 3. Gửi danh sách đã chốt giá lên Server để Broadcast
                DataManager.saveProducts(allProducts);
                System.out.println("[CLIENT] Đã gửi giá đấu mới lên Server!");

            } catch (Exception e) {
                showError("Lỗi mạng", "Không thể đồng bộ giá lên máy chủ!");
                return;
            }

            // Báo thành công (ĐOẠN NÀY PHẢI NẰM TRONG TRY LỚN)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Bạn đã ra giá thành công!");
            alert.showAndWait();

            // Tự động đóng cửa sổ Pop-up
            Stage stage = (Stage) bidAmountField.getScene().getWindow();
            stage.close();

            // Bắt các lỗi
        } catch (NumberFormatException e) {
            showError("Lỗi định dạng", "Vui lòng chỉ nhập số (Ví dụ: 25000000)");
        } catch (InvalidBidException | AuctionClosedException e) {
            // Hứng trọn 2 cái lỗi do chính mình ném ra
            showError("Lỗi hệ thống", e.getMessage());
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