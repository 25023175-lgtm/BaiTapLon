package com.uet.auction;

import com.auction.model.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

    private Item currentProduct;

    // 1. Nhận dữ liệu từ Dashboard
    public void setProductData(Item product) {
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

            // --- SYNCHRONIZED: Khoa doi tuong de tranh race condition ---
            // Khi 2 client dat gia cung luc, chi 1 thread duoc vao khoi nay
            synchronized (currentProduct) {

                // --- KIỂM TRA LUẬT ĐẤU GIÁ ---
                if (currentProduct.getStatus() != null
                        && !currentProduct.getStatus().equals("ACTIVE")) {
                    throw new AuctionClosedException(
                            "Món đồ này đã chốt đơn, không thể ra giá thêm!");
                }

                if (bidAmount <= currentProduct.getCurrentPrice()) {
                    throw new InvalidBidException(
                            "Số tiền đấu giá phải cao hơn giá hiện tại!");
                }

                // --- THỰC HIỆN CẬP NHẬT DỮ LIỆU ---
                currentProduct.setCurrentPrice(bidAmount);
                currentProduct.setBidCount(currentProduct.getBidCount() + 1);

                // LOGIC ANTI-SNIPING
                LocalDateTime now = LocalDateTime.now();
                Duration timeLeft = Duration.between(now, currentProduct.getEndTime());
                if (timeLeft.getSeconds() <= 60 && timeLeft.getSeconds() > 0) {
                    currentProduct.setEndTime(
                            currentProduct.getEndTime().plusMinutes(5));
                    System.out.println("[ANTI-SNIPING] Đã gia hạn 5 phút cho: "
                            + currentProduct.getName());
                }

                DataManager.updateProduct(currentProduct);
                DataManager.saveBid(currentProduct.getId(), bidAmount);

            } // --- KET THUC SYNCHRONIZED BLOCK ---

            // ── Thông báo thành công (styled) ─────────────────────────
            showSuccess(currentProduct.getName(), bidAmount);

            // Đóng cửa sổ Pop-up
            Stage stage = (Stage) bidAmountField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("Lỗi định dạng", "Vui lòng chỉ nhập số.\nVí dụ: 25000000");
        } catch (InvalidBidException | AuctionClosedException e) {
            showError("Không hợp lệ", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi mạng", "Không thể đồng bộ giá lên máy chủ!");
        }
    }

    // ── Alert thành công (xanh lá) ────────────────────────────────────
    private void showSuccess(String productName, double amount) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Đặt giá thành công");
        alert.setHeaderText(null);

        // Header xanh lá
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(6);
        header.setStyle(
                "-fx-background-color: #2D6A4F;" +
                        "-fx-padding: 22 24 20 24;" +
                        "-fx-alignment: CENTER;"
        );
        javafx.scene.control.Label icon = new javafx.scene.control.Label("✅");
        icon.setStyle("-fx-font-size: 32px;");
        javafx.scene.control.Label title = new javafx.scene.control.Label("Đặt giá thành công!");
        title.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;"
        );
        header.getChildren().addAll(icon, title);

        // Body trắng
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(10);
        body.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 20 24 16 24;" +
                        "-fx-alignment: CENTER;"
        );

        javafx.scene.control.Label prodLabel = new javafx.scene.control.Label(
                "Sản phẩm: " + productName
        );
        prodLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        String fmtAmount = String.format("%,.0f VND", amount).replace(",", ".");
        javafx.scene.control.Label amountLabel = new javafx.scene.control.Label(fmtAmount);
        amountLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A2E22;"
        );

        javafx.scene.control.Label subLabel = new javafx.scene.control.Label(
                "là mức giá cao nhất hiện tại 🎉"
        );
        subLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        body.getChildren().addAll(prodLabel, amountLabel, subLabel);

        // Assemble
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(360);
        root.getChildren().addAll(header, body);

        alert.getDialogPane().setContent(root);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;");

        ButtonType btnOk = new ButtonType("Tuyệt vời!", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnOk);

        alert.setOnShown(ev -> {
            javafx.scene.Node btn = alert.getDialogPane().lookupButton(btnOk);
            if (btn != null) btn.setStyle(
                    "-fx-background-color: #2D6A4F; -fx-text-fill: white;" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 9 28;"
            );
        });

        alert.showAndWait();
    }

    // ── Alert lỗi (đỏ) ───────────────────────────────────────────────
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Header đỏ
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(6);
        header.setStyle(
                "-fx-background-color: #DC2626;" +
                        "-fx-padding: 18 24 16 24;" +
                        "-fx-alignment: CENTER;"
        );
        javafx.scene.control.Label icon = new javafx.scene.control.Label("❌");
        icon.setStyle("-fx-font-size: 26px;");
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;"
        );
        header.getChildren().addAll(icon, titleLabel);

        // Body trắng
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(0);
        body.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 18 24 12 24;" +
                        "-fx-alignment: CENTER;"
        );
        javafx.scene.control.Label msg = new javafx.scene.control.Label(content);
        msg.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #374151; -fx-wrap-text: true;");
        msg.setWrapText(true);
        body.getChildren().add(msg);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(340);
        root.getChildren().addAll(header, body);

        alert.getDialogPane().setContent(root);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;");

        ButtonType btnOk = new ButtonType("Đã hiểu", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnOk);

        alert.setOnShown(ev -> {
            javafx.scene.Node btn = alert.getDialogPane().lookupButton(btnOk);
            if (btn != null) btn.setStyle(
                    "-fx-background-color: #DC2626; -fx-text-fill: white;" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 9 24;"
            );
        });

        alert.showAndWait();
    }
}