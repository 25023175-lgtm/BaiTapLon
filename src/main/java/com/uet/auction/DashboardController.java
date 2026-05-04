package com.uet.auction;

import javafx.scene.control.*;
import com.auction.model.Product; // Import class Product của Thành viên A
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.event.ActionEvent;

import java.awt.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;


import javax.swing.*;


public class DashboardController implements Initializable {

    // 1. Khai báo các thành phần đã đặt fx:id trong Scene Builder
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colStartPrice;
    @FXML private HBox addProductBox;
    @FXML private TableColumn<Product, Double> colCurrentPrice;
    @FXML private TableColumn<Product, String> colStatus;

    // Danh sách quan sát (sẽ tự động cập nhật bảng khi có thay đổi)
    private ObservableList<Product> productList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Chỉ định nối cột
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Làm đẹp cột Giá (Đã sửa lại cú pháp chuẩn của JavaFX)
        colStartPrice.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", price).replace(",", "."));
                }
            }
        });

        colCurrentPrice.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", price).replace(",", "."));
                }
            }
        });

        // 3. Phân quyền Bidder / Seller
        if (SessionManager.getInstance().getCurrentUser() != null) {
            String role = SessionManager.getInstance().getCurrentUser().getRole();

            if (role.equals("Bidder")) {
                // Người mua: Giấu thanh thêm sản phẩm
                if (addProductBox != null) {
                    addProductBox.setVisible(false);
                    addProductBox.setManaged(false);
                }
                // Click đúp để mở Pop-up đấu giá
                productTable.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && productTable.getSelectionModel().getSelectedItem() != null) {
                        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
                        openBidWindow(selectedProduct);
                    }
                });

            } else if (role.equals("Seller")) {
                // Người bán: Tắt tính năng click đúp đấu giá, hiện thanh thêm sản phẩm
                productTable.setOnMouseClicked(null);
                if (addProductBox != null) {
                    addProductBox.setVisible(true);
                    addProductBox.setManaged(true);
                }
            }
        }

        // 4. TRÙM CUỐI: Bật bộ đàm Realtime hóng dữ liệu từ Server
        DataManager.startRealtimeListener(this);
    }



// 1. Khai báo 2 ô nhập liệu mới gắn bên Scene Builder
@FXML
private TextField newNameField;
@FXML
private TextField newPriceField;

// 2. Viết hàm xử lý khi bấm nút "Thêm sản phẩm"
@FXML
public void handleAddProduct() {
    try {
        // Lấy tên và giá từ 2 ô nhập liệu
        String name = newNameField.getText();
        double price = Double.parseDouble(newPriceField.getText());

        // 1. Tạo sản phẩm mới
        com.auction.model.Product newProduct = new com.auction.model.Product();
        newProduct.setName(name);
        newProduct.setStartPrice(price);
        newProduct.setCurrentPrice(price);
        newProduct.setStatus("ACTIVE");

        // 2. [ĐÃ VÁ LỖI Ở ĐÂY]: Kiểm tra nếu danh sách đang trống thì phải khởi tạo nó trước
        if (productList == null) {
            productList = javafx.collections.FXCollections.observableArrayList();
            productTable.setItems(productList); // Nối nó vào bảng
        }

        // 3. Lấy danh sách hiện tại, nhét thêm đồ mới vào
        java.util.List<com.auction.model.Product> currentList = new java.util.ArrayList<>(productList);
        currentList.add(newProduct);

        // 4. Gửi dữ liệu mới cập nhật cho Server
        DataManager.saveProducts(currentList);

        // Xóa trắng ô nhập liệu cho đẹp
        newNameField.clear();
        newPriceField.clear();

    } catch (NumberFormatException e) {
        // Bắt riêng lỗi nhập sai chữ vào ô giá tiền
        System.out.println("Vui lòng nhập đúng định dạng số cho giá tiền!");
    } catch (Exception e) {
        // Nếu có lỗi khác thì in ra dòng đỏ để mình còn biết đường sửa
        e.printStackTrace();
    }
}

    @FXML
    public void handleDeleteProduct() {
        // 1. Lấy ra cái sản phẩm mà người dùng đang click chọn trên bảng
        com.auction.model.Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        // 2. Kiểm tra xem họ có thực sự chọn món nào không
        if (selectedProduct != null) {
            // Xóa món đó khỏi danh sách hiển thị
            productList.remove(selectedProduct);

            // Gửi danh sách mới (đã bị xóa mất 1 món) lên Server để lưu lại
            java.util.List<com.auction.model.Product> currentList = new java.util.ArrayList<>(productList);
            DataManager.saveProducts(currentList);

        } else {
            // 3. Nếu bấm Xóa mà chưa chọn món nào thì hiện bảng cảnh báo cực xịn
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng click chọn một sản phẩm trong bảng trước khi bấm nút Xóa!");
            alert.showAndWait();
        }
    }

// Hàm mở cửa sổ Pop-up
private void openBidWindow(Product product) {
    try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("bid-view.fxml"));
        javafx.scene.Parent root = loader.load();

        // Gửi dữ liệu món đồ sang cho BidController
        BidController controller = loader.getController();
        controller.setProductData(product);

        // Mở cửa sổ mới
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Ra giá sản phẩm");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();

        // MẸO CỰC HAY: Khi cửa sổ Pop-up tắt đi, ép cái bảng phải làm mới lại dữ liệu!
        stage.setOnHidden(e -> {
            productTable.refresh(); // Cập nhật hiển thị giá mới trên bảng
            DataManager.saveProducts(productList); // Lưu giá mới này xuống file
        });

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void refreshTable(List<com.auction.model.Product> newList) {
        // Cần đảm bảo cập nhật UI trên luồng chính của JavaFX
        javafx.application.Platform.runLater(() -> {
            if (productList == null) {
                productList = javafx.collections.FXCollections.observableArrayList();
                productTable.setItems(productList); // Nối danh sách vào bảng
            }
            productList.setAll(newList); // Đổ dữ liệu mới từ Server vào
        });
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // 1. Xóa thông tin phiên làm việc hiện tại
        SessionManager.getInstance().logout();
        System.out.println("Đã xóa thông tin user, chuẩn bị đăng xuất...");

        // 2. Ngắt kết nối socket (nếu DataManager có hàm ngắt kết nối thì bỏ comment dòng dưới)
        // DataManager.stopRealtimeListener();

        // 3. Chuyển màn hình về trang đăng nhập
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá UET - Đăng nhập");
    }


}
