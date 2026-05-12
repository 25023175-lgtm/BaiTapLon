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
import java.time.LocalDateTime;
import java.time.Duration;

public class BidController {

    @FXML private Label productNameLabel;
    @FXML private Label currentPriceLabel;
    @FXML private TextField bidAmountField;

    private Product currentProduct;

    // 1. Nhận dữ liệu từ Dashboard
    public void setProductData(Product product) {
        this.currentProduct = product;
        productNameLabel.setText("Đang đấu giá: " + product.getName());

        String formattedPrice = String.format("%,.0f", product.getCurrentPrice()).replace(",", ".");
        currentPriceLabel.setText("Giá hiện tại: " + formattedPrice + " VNĐ");
    }

    // 2. Xử lý Ra giá
    @FXML
    void handlePlaceBid(ActionEvent event) {
        try {
            double bidAmount = Double.parseDouble(bidAmountField.getText());

            // --- KIỂM TRA LUẬT ĐẤU GIÁ ---
            // Kiểm tra trạng thái
            if (currentProduct.getStatus() != null && !currentProduct.getStatus().equals("ACTIVE")) {
                throw new AuctionClosedException("Món đồ này đã chốt đơn, không thể ra giá thêm!");
            }

            // Kiểm tra giá tiền
            if (bidAmount <= currentProduct.getCurrentPrice()) {
                throw new InvalidBidException("Số tiền đấu giá phải cao hơn giá hiện tại!");
            }

            // --- THỰC HIỆN CẬP NHẬT DỮ LIỆU ---

            // 1. Cập nhật giá mới và số lượt Bid trong RAM
            currentProduct.setCurrentPrice(bidAmount);
            currentProduct.setBidCount(currentProduct.getBidCount() + 1);

            // 2. LOGIC ANTI-SNIPING (CHỐNG BẮN TỈA)
            LocalDateTime now = LocalDateTime.now();
            Duration timeLeft = Duration.between(now, currentProduct.getEndTime());

            // Nếu thời gian còn lại <= 60 giây và chưa kết thúc
            if (timeLeft.getSeconds() <= 60 && timeLeft.getSeconds() > 0) {
                // Gia hạn thêm 5 phút
                currentProduct.setEndTime(currentProduct.getEndTime().plusMinutes(5));
                System.out.println(">> [ANTI-SNIPING] Đã gia hạn 5 phút cho: " + currentProduct.getName());
            }

            // 3. ĐẨY DỮ LIỆU XUỐNG DATABASE
            // Lưu giá mới, số lượt bid mới và thời gian kết thúc mới (nếu có cộng giờ)
            DataManager.updateProduct(currentProduct);

            // Lưu lịch sử nhát búa này vào bảng bids
            DataManager.saveBid(currentProduct.getId(), bidAmount);

            // Báo thành công cho người dùng
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Bạn đã ra giá thành công!");
            alert.showAndWait();

            // Đóng cửa sổ Pop-up
            Stage stage = (Stage) bidAmountField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("Lỗi định dạng", "Vui lòng chỉ nhập số (Ví dụ: 25000000)");
        } catch (InvalidBidException | AuctionClosedException e) {
            showError("Lỗi hệ thống", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi mạng", "Không thể đồng bộ giá lên máy chủ!");
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