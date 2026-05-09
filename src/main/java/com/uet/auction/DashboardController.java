package com.uet.auction;

import com.auction.model.Product;
import com.auction.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // 1. Khai báo các cột trong bảng (Đã cập nhật đủ 5 cột theo FXML mới)
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colStartPrice;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, LocalDateTime> colStartTime;
    @FXML private TableColumn<Product, LocalDateTime> colEndTime;

    // 2. Khai báo các ô nhập liệu của Seller
    @FXML private HBox addProductBox;
    @FXML private TextField newNameField;
    @FXML private TextField newDescField;
    @FXML private TextField newPriceField;
    @FXML private TextField newDurationField;


    @FXML private Label totalProductsLabel;
    @FXML private Label highestPriceLabel;
    @FXML private Label highestPriceNameLabel;
    @FXML private Label mostBidsLabel;
    @FXML private Label mostBidsNameLabel;

    // Danh sách quan sát
    private ObservableList<Product> productList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Chỉ định nối cột
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // 2. Làm đẹp cột Giá (Có dấu phẩy phân cách)
        colStartPrice.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", price).replace(",", "."));
                }
            }
        });

        // 3. Làm đẹp định dạng Thời gian cho 2 cột StartTime và EndTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        colStartTime.setCellFactory(column -> new TableCell<Product, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.format(formatter));
                }
            }
        });

        colEndTime.setCellFactory(column -> new TableCell<Product, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.format(formatter));
                }
            }
        });

        // 4. Phân quyền Bidder / Seller
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            String role = currentUser.getRole();

            if ("Bidder".equals(role)) {
                // Người mua: Giấu thanh thêm sản phẩm
                if (addProductBox != null) {
                    addProductBox.setVisible(false);
                    addProductBox.setManaged(false);
                }

                // Click đúp để mở Pop-up đấu giá HOẶC bảng chi tiết
                productTable.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && productTable.getSelectionModel().getSelectedItem() != null) {
                        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
                        showBidderProductDetail(selectedProduct);;
                    }
                });

            } else if ("Seller".equals(role)) {
                // Người bán: Hiện thanh thêm sản phẩm, không cho mở BidWindow
                if (addProductBox != null) {
                    addProductBox.setVisible(true);
                    addProductBox.setManaged(true);
                }
                productTable.setOnMouseClicked(null);
            }
        }

        // 5. Bật bộ đàm Realtime nghe dữ liệu từ Server
        DataManager.startRealtimeListener(this);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ CỦA SELLER
    // ==========================================
    @FXML
    public void handleAddProduct() {
        try {
            // Lấy dữ liệu từ các ô
            String name = newNameField.getText();
            String desc = newDescField.getText();
            double price = Double.parseDouble(newPriceField.getText());
            int durationMinutes = Integer.parseInt(newDurationField.getText());

            // Tạo sản phẩm mới
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setDescription(desc);
            newProduct.setStartPrice(price);
            newProduct.setCurrentPrice(price);
            newProduct.setStatus("ACTIVE");

            // Gán Thời gian
            LocalDateTime now = LocalDateTime.now();
            newProduct.setStartTime(now);
            newProduct.setEndTime(now.plusMinutes(durationMinutes));

            // Gán Người bán từ Session
            User loggedInUser = SessionManager.getInstance().getCurrentUser();
            if (loggedInUser != null) {
                newProduct.setSellerName(loggedInUser.getFullName());
                newProduct.setSellerId(loggedInUser.getId());
            } else {
                newProduct.setSellerName("Người ẩn danh");
            }

            // Khởi tạo list nếu null
            if (productList == null) {
                productList = FXCollections.observableArrayList();
                productTable.setItems(productList);
            }

            // Thêm vào list và lưu
            List<Product> currentList = new ArrayList<>(productList);
            currentList.add(newProduct);
            DataManager.saveProducts(currentList);

            // Xóa trắng ô nhập liệu
            newNameField.clear();
            newDescField.clear();
            newPriceField.clear();
            newDurationField.clear();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập định dạng số cho Giá tiền và Thời gian (Phút)!");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteProduct() {
        // 1. Lấy ra sản phẩm mà người dùng đang click chọn trên bảng
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {

            // 2. LẤY THÔNG TIN NGƯỜI ĐANG ĐĂNG NHẬP
            User loggedInUser = SessionManager.getInstance().getCurrentUser();

            // 3. KIỂM TRA QUYỀN SỞ HỮU
            // So sánh tên người đăng nhập với tên người bán của sản phẩm
            if (loggedInUser != null && selectedProduct.getSellerName().equalsIgnoreCase(loggedInUser.getFullName())) {

                // Trùng tên -> Đúng là chính chủ -> Cho phép xóa
                productList.remove(selectedProduct);

                // Cập nhật lại danh sách sau khi xóa
                List<Product> currentList = new ArrayList<>(productList);
                DataManager.saveProducts(currentList);

                System.out.println("Đã xóa thành công sản phẩm của bạn!");

            } else {
                // Không trùng tên -> Cảnh báo ngay lập tức
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi phân quyền");
                alert.setHeaderText("Hành động bị từ chối!");
                alert.setContentText("Bạn không thể xóa sản phẩm của người khác.\nBạn chỉ có quyền xóa những mặt hàng do chính bạn đăng bán.");
                alert.showAndWait();
            }

        } else {
            // Trường hợp bấm nút xóa nhưng chưa chọn món nào
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng click chọn một sản phẩm trong bảng trước khi bấm nút Xóa!");
            alert.showAndWait();
        }
    }

    // ==========================================
    // CÁC HÀM CHUNG KHÁC
    // ==========================================
    private void openBidWindow(Product product) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("bid-view.fxml"));
            javafx.scene.Parent root = loader.load();

            BidController controller = loader.getController();
            controller.setProductData(product);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Chi tiết / Ra giá sản phẩm");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

            stage.setOnHidden(e -> {
                productTable.refresh();
                if(productList != null) {
                    DataManager.saveProducts(new ArrayList<>(productList));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardStats() {
        // Đảm bảo cập nhật giao diện trên luồng chính của JavaFX
        javafx.application.Platform.runLater(() -> {
            if (productList == null || productList.isEmpty()) {
                totalProductsLabel.setText("0");
                highestPriceLabel.setText("0 VND");
                highestPriceNameLabel.setText("Chưa có sản phẩm");
                mostBidsLabel.setText("0 lượt");
                mostBidsNameLabel.setText("Chưa có sản phẩm");
                return;
            }

            // 1. Gắn tổng số sản phẩm
            totalProductsLabel.setText(String.valueOf(productList.size()));

            // 2. Tìm giá cao nhất và nhiều lượt bid nhất
            Product highestPriceProduct = productList.get(0);
            Product mostBidsProduct = productList.get(0);

            for (Product p : productList) {
                // So sánh để tìm Giá cao nhất
                if (p.getCurrentPrice() > highestPriceProduct.getCurrentPrice()) {
                    highestPriceProduct = p;
                }

                // So sánh để tìm Nhiều lượt đấu giá nhất
                if (p.getBidCount() > mostBidsProduct.getBidCount()) {
                    mostBidsProduct = p;
                }
            }

            // 3. Cập nhật ô Giá cao nhất
            highestPriceLabel.setText(String.format("%,.0f VND", highestPriceProduct.getCurrentPrice()));
            highestPriceNameLabel.setText(highestPriceProduct.getName());

            // 4. Cập nhật ô Nhiều lượt Bid nhất
            mostBidsLabel.setText(mostBidsProduct.getBidCount() + " lượt");
            mostBidsNameLabel.setText(mostBidsProduct.getName());
        });
    }

    public void refreshTable(List<Product> newList) {
        Platform.runLater(() -> {
            if (productList == null) {
                productList = FXCollections.observableArrayList();
                productTable.setItems(productList);
            }
            productList.setAll(newList);

            updateDashboardStats();
        });
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá UET - Đăng nhập");
    }

    // Hàm hiển thị chi tiết cho Người mua (Đã fix lỗi hiển thị và đếm ngược Real-time)
    private void showBidderProductDetail(Product product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông tin sản phẩm");
        alert.setHeaderText("Mặt hàng: " + product.getName().toUpperCase());

        // 1. Tạo một cái Label xịn xò để thay thế vùng hiển thị mặc định
        javafx.scene.control.Label contentLabel = new javafx.scene.control.Label();
        contentLabel.setStyle("-fx-font-size: 14px;");
        contentLabel.setWrapText(true);
        contentLabel.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE); // Ép Label tự phình to theo chữ
        alert.getDialogPane().setContent(contentLabel);

        // 2. Setup nút bấm
        ButtonType btnClose = new ButtonType("Đóng", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnBid = new ButtonType("Ra giá ngay!", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnBid, btnClose);

        // 3. Đóng gói logic tính toán vào một khối lệnh (Runnable) để tái sử dụng
        Runnable updateTimeLogic = () -> {
            java.time.Duration duration = java.time.Duration.between(java.time.LocalDateTime.now(), product.getEndTime());
            String timeRemaining;
            boolean isEnded = false;

            if (duration.isNegative() || duration.isZero()) {
                timeRemaining = "Phiên đấu giá đã kết thúc!";
                isEnded = true;
            } else {
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;

                if (hours > 0) {
                    timeRemaining = hours + " giờ " + minutes + " phút " + seconds + " giây";
                } else {
                    timeRemaining = minutes + " phút " + seconds + " giây";
                }
            }

            String desc = (product.getDescription() != null && !product.getDescription().isEmpty())
                    ? product.getDescription() : "Không có mô tả chi tiết.";

            String content = "Người bán: " + product.getSellerName() + "\n"
                    + "-----------------------------------\n"
                    + "Mô tả: " + desc + "\n"
                    + "-----------------------------------\n"
                    + "Giá khởi điểm: " + String.format("%,.0f VND", product.getStartPrice()) + "\n"
                    + "Giá hiện tại (Cao nhất): " + String.format("%,.0f VND", product.getCurrentPrice()) + "\n"
                    + "-----------------------------------\n"
                    + "Thời gian còn lại: " + timeRemaining;

            // Đổ chữ vào Label
            contentLabel.setText(content);

            // Khóa nút nếu hết giờ
            javafx.scene.Node bidButton = alert.getDialogPane().lookupButton(btnBid);
            if (bidButton != null) {
                bidButton.setDisable(isEnded);
            }
        };

        // 4. GỌI LỆNH LẦN 1
        // Để bảng có sẵn chữ, tính toán xong kích thước rồi mới hiện lên
        updateTimeLogic.run();

        // 5.Timeline
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    updateTimeLogic.run(); // Cứ 1 giây lại gọi khối lệnh tính giờ 1 lần
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

        alert.setOnHidden(e -> timeline.stop()); // Tắt bảng thì dừng đồng hồ
        alert.getDialogPane().setMinWidth(420);

        // Hiển thị bảng
        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == btnBid) {
            openBidWindow(product);
        }
    }
}
