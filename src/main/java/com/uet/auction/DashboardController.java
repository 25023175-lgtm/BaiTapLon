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
        if (SessionManager.currentUser != null) {
            String role = SessionManager.currentUser.getRole();

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
        // (Chú ý: Ở ảnh trước tôi thấy bạn đặt tên biến bị sai chính tả là newNaneField, nếu đúng thế thì sửa lại chữ N thành M ở đây nhé)
        String name = newNameField.getText();
        double price = Double.parseDouble(newPriceField.getText()); // Thay bằng tên cái biến ô nhập giá của bạn

        // 1. Tạo sản phẩm mới
        // 1. Tạo một sản phẩm bằng khuôn rỗng (không truyền gì cả)
        com.auction.model.Product newProduct = new com.auction.model.Product();

        // 2. Dùng các hàm Setters để "bơm" từ từ từng thông tin vào
        newProduct.setName(name);                 // Bơm tên
        newProduct.setStartPrice(price);          // Bơm giá khởi điểm
        newProduct.setCurrentPrice(price);        // Giá hiện tại bằng luôn giá khởi điểm
        newProduct.setStatus("ACTIVE");           // Đặt trạng thái là đang đấu giá

        // Các biến khác (description, endTime...) bạn không gọi set thì Java sẽ tự động để trống (null hoặc 0)

        // 2. Lấy danh sách hiện tại, nhét thêm đồ mới vào
        java.util.List<com.auction.model.Product> currentList = new java.util.ArrayList<>(productList);
        currentList.add(newProduct);

        // 3. GỌI ĐIỆN CHO SERVER: Gửi bản cập nhật lên để Server Broadcast cho tất cả mọi người
        DataManager.saveProducts(currentList);

        // Xóa trắng ô nhập liệu cho đẹp
        newNameField.clear();
        newPriceField.clear();

    } catch (Exception e) {
        System.out.println("Vui lòng nhập đúng định dạng số cho giá tiền!");
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


}
