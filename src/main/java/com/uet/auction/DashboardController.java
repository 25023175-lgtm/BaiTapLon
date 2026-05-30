package com.uet.auction;

import com.auction.factory.ItemFactory;
import com.auction.model.Item;
import com.auction.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // 1. Khai báo các cột trong bảng
    @FXML private TableView<Item> productTable;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, Double> colStartPrice;
    @FXML private TableColumn<Item, String> colDescription;
    @FXML private TableColumn<Item, LocalDateTime> colStartTime;
    @FXML private TableColumn<Item, LocalDateTime> colEndTime;

    // 2. Khai báo các ô nhập liệu của Seller
    @FXML private javafx.scene.layout.VBox addProductBox;
    @FXML private javafx.scene.layout.HBox depositBox;
    @FXML private Label balanceLabel;
    @FXML private TextField newNameField;
    @FXML private TextField newDescField;
    @FXML private TextField newPriceField;
    @FXML private TextField newDurationField;
    @FXML private javafx.scene.control.ComboBox<String> categoryComboBox;

    @FXML private Label totalProductsLabel;
    @FXML private Label highestPriceLabel;
    @FXML private Label highestPriceNameLabel;
    @FXML private Label mostBidsLabel;
    @FXML private Label mostBidsNameLabel;

    // 3. Khai báo các item sidebar để xử lý active state
    @FXML private HBox navDashboard;
    @FXML private HBox navProducts;
    @FXML private HBox navCalendar;
    @FXML private HBox navNotifications;
    @FXML private HBox navSettings;
    @FXML private HBox navHelp;

    // Style constants cho sidebar item
    private static final String STYLE_ACTIVE =
            "-fx-background-color: #2D6A4F; -fx-cursor: hand; -fx-padding: 11 20;";
    private static final String STYLE_INACTIVE =
            "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 11 20;";

    // Danh sách quan sát
    private ObservableList<Item> productList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Chỉ định nối cột
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime")); // Da fix: dung startTime chinh xac
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // 2. Làm đẹp cột Giá (Có dấu phẩy phân cách)
        colStartPrice.setCellFactory(column -> new TableCell<Item, Double>() {
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

        colStartTime.setCellFactory(column -> new TableCell<Item, LocalDateTime>() {
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

        colEndTime.setCellFactory(column -> new TableCell<Item, LocalDateTime>() {
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

        // Cap nhat hien thi so du
        updateBalanceLabel();

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
                        Item selectedProduct = productTable.getSelectionModel().getSelectedItem();
                        showBidderProductDetail(selectedProduct);
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

        // 5. KÉO DỮ LIỆU TỪ MYSQL VÀ ĐỔ VÀO BẢNG
        loadDataFromDatabase();

        // 6. Bật bộ đàm Realtime nghe dữ liệu từ Server (Đã bật lại)
        DataManager.startRealtimeListener(this);

        // Them vao cuoi ham initialize()
        categoryComboBox.getItems().addAll(
                "General", "Electronics", "Art", "Vehicle"
        );
        categoryComboBox.setValue("General"); // Mac dinh chon General
    }

    // ==========================================
    // HÀM TẢI LẠI TOÀN BỘ DỮ LIỆU CHUẨN
    // ==========================================
    private void loadDataFromDatabase() {
        List<Item> products = DataManager.loadProducts();
        productList = FXCollections.observableArrayList(products);
        productTable.setItems(productList);

        // Gọi hàm thống kê lại bảng điều khiển sau khi kéo data
        updateDashboardStats();
    }



    // ==========================================
    // NAP TIEN
    // ==========================================
    @FXML
    public void handleDeposit() {
        com.auction.model.User currentUser =
                SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Dialog nap tien co giao dien dep
        javafx.scene.control.Dialog<String> dialog =
                new javafx.scene.control.Dialog<>();
        dialog.setTitle("Nạp tiền vào tài khoản");

        // Header xanh la
        javafx.scene.layout.VBox dHeader = new javafx.scene.layout.VBox(6);
        dHeader.setStyle("-fx-background-color: #2D6A4F;"
                + "-fx-padding: 20 24 16 24; -fx-alignment: CENTER;");
        javafx.scene.control.Label dIcon =
                new javafx.scene.control.Label("💰");
        dIcon.setStyle("-fx-font-size: 30px;");
        javafx.scene.control.Label dTitle =
                new javafx.scene.control.Label("Nạp tiền vào tài khoản");
        dTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;"
                + "-fx-text-fill: white;");
        javafx.scene.control.Label dBalance =
                new javafx.scene.control.Label(
                        "Số dư hiện tại: "
                                + String.format("%,.0f VNĐ", currentUser.getBalance())
                                .replace(",", "."));
        dBalance.setStyle("-fx-font-size: 12px; -fx-text-fill: #A7F3D0;");
        dHeader.getChildren().addAll(dIcon, dTitle, dBalance);

        // Body
        javafx.scene.layout.VBox dBody = new javafx.scene.layout.VBox(10);
        dBody.setStyle("-fx-background-color: white;"
                + "-fx-padding: 20 24 8 24;");
        javafx.scene.control.Label dLabel =
                new javafx.scene.control.Label("Nhập số tiền muốn nạp (VNĐ):");
        dLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        javafx.scene.control.TextField dInput =
                new javafx.scene.control.TextField("100000");
        dInput.setStyle("-fx-font-size: 14px; -fx-pref-height: 40px;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: #D1FAE5; -fx-border-radius: 8;");
        dBody.getChildren().addAll(dLabel, dInput);

        // Assemble
        javafx.scene.layout.VBox dRoot = new javafx.scene.layout.VBox(0);
        dRoot.setStyle("-fx-background-color: white;");
        dRoot.setPrefWidth(340);
        dRoot.getChildren().addAll(dHeader, dBody);

        dialog.getDialogPane().setContent(dRoot);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: white; -fx-padding: 0;");

        javafx.scene.control.ButtonType btnOk =
                new javafx.scene.control.ButtonType("Nạp tiền",
                        javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType btnCancel =
                new javafx.scene.control.ButtonType("Huỷ",
                        javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(btnOk, btnCancel);

        dialog.setResultConverter(btn -> {
            if (btn == btnOk) return dInput.getText();
            return null;
        });

        dialog.setOnShown(ev -> {
            javafx.scene.Node okNode =
                    dialog.getDialogPane().lookupButton(btnOk);
            if (okNode != null) okNode.setStyle(
                    "-fx-background-color: #2D6A4F; -fx-text-fill: white;"
                            + "-fx-font-weight: bold; -fx-background-radius: 8;"
                            + "-fx-cursor: hand; -fx-padding: 8 20;");
            javafx.scene.Node cancelNode =
                    dialog.getDialogPane().lookupButton(btnCancel);
            if (cancelNode != null) cancelNode.setStyle(
                    "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;"
                            + "-fx-background-radius: 8; -fx-cursor: hand;"
                            + "-fx-padding: 8 20;");
        });

        dialog.showAndWait().ifPresent(input -> {
            if (input == null) return;
            try {
                double amount = Double.parseDouble(
                        input.trim().replace(".", "").replace(",", ""));

                if (amount <= 0) {
                    showNapTienError("Số tiền phải lớn hơn 0!");
                    return;
                }

                if (amount > 1_000_000_000) {
                    showNapTienError("Số tiền nạp tối đa là 1.000.000.000 VNĐ!");
                    return;
                }

                // Cap nhat so du
                double newBalance = currentUser.getBalance() + amount;
                currentUser.setBalance(newBalance);
                DataManager.updateBalance(currentUser.getId(), newBalance);

                // Cap nhat hien thi
                updateBalanceLabel();

                // Thong bao thanh cong
                javafx.scene.control.Alert alert =
                        new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.NONE);
                alert.setTitle("Nạp tiền thành công");
                alert.setHeaderText(null);

                javafx.scene.layout.VBox header =
                        new javafx.scene.layout.VBox(6);
                header.setStyle("-fx-background-color: #2D6A4F;"
                        + "-fx-padding: 20 24 16 24; -fx-alignment: CENTER;");
                javafx.scene.control.Label icon =
                        new javafx.scene.control.Label("💰");
                icon.setStyle("-fx-font-size: 30px;");
                javafx.scene.control.Label title =
                        new javafx.scene.control.Label("Nạp tiền thành công!");
                title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;"
                        + "-fx-text-fill: white;");
                header.getChildren().addAll(icon, title);

                javafx.scene.layout.VBox body =
                        new javafx.scene.layout.VBox(6);
                body.setStyle("-fx-background-color: white;"
                        + "-fx-padding: 16 24 12 24; -fx-alignment: CENTER;");
                javafx.scene.control.Label added =
                        new javafx.scene.control.Label(
                                "+ " + String.format("%,.0f VNĐ", amount)
                                        .replace(",", "."));
                added.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;"
                        + "-fx-text-fill: #2D6A4F;");
                javafx.scene.control.Label total =
                        new javafx.scene.control.Label(
                                "Số dư mới: "
                                        + String.format("%,.0f VNĐ", newBalance)
                                        .replace(",", "."));
                total.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
                body.getChildren().addAll(added, total);

                javafx.scene.layout.VBox root =
                        new javafx.scene.layout.VBox(0);
                root.setStyle("-fx-background-color: white;");
                root.setPrefWidth(320);
                root.getChildren().addAll(header, body);

                alert.getDialogPane().setContent(root);
                alert.getDialogPane().setStyle(
                        "-fx-background-color: white; -fx-padding: 0;");

                javafx.scene.control.ButtonType btnSuccess =
                        new javafx.scene.control.ButtonType("Tuyệt vời!",
                                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(btnSuccess);
                alert.setOnShown(ev -> {
                    javafx.scene.Node btn =
                            alert.getDialogPane().lookupButton(btnSuccess);
                    if (btn != null) btn.setStyle(
                            "-fx-background-color: #2D6A4F; -fx-text-fill: white;"
                                    + "-fx-font-weight: bold; -fx-background-radius: 8;"
                                    + "-fx-cursor: hand; -fx-padding: 8 24;");
                });
                alert.showAndWait();

            } catch (NumberFormatException e) {
                showNapTienError("Vui lòng nhập số hợp lệ!");
            }
        });
    }

    private void updateBalanceLabel() {
        if (balanceLabel == null) return;
        com.auction.model.User user =
                SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        String fmt = String.format("%,.0f", user.getBalance())
                .replace(",", ".");
        balanceLabel.setText(fmt + " VNĐ");
    }

    private void showNapTienError(String msg) {
        javafx.scene.control.Alert err =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
        err.setTitle("Lỗi nạp tiền");
        err.setHeaderText(null);
        err.setContentText(msg);
        err.showAndWait();
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ CỦA SELLER
    // ==========================================
    @FXML
    public void handleAddProduct() {
        try {
            // Lay du lieu tu cac o nhap lieu
            String name = newNameField.getText();
            String desc = newDescField.getText();
            double price = Double.parseDouble(newPriceField.getText());
            int durationMinutes = Integer.parseInt(newDurationField.getText());

            // Lay category tu ComboBox (mac dinh General neu chua chon)
            String category = categoryComboBox.getValue();
            if (category == null || category.isEmpty()) {
                category = "General";
            }

            // Tao san pham dung ItemFactory theo category nguoi ban chon
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.plusMinutes(durationMinutes);
            Item newProduct = ItemFactory.create(
                    category, name, desc, price, 0.0, endTime, 0);

            // Gan nguoi ban tu Session
            User loggedInUser = SessionManager.getInstance().getCurrentUser();
            if (loggedInUser != null) {
                newProduct.setSellerId(loggedInUser.getId());
                newProduct.setSellerName(loggedInUser.getFullName());
            }

            // 1. Cap nhat len giao dien
            productList.add(newProduct);

            // 2. Luu vao Database
            DataManager.saveProduct(newProduct);

            // 3. Xoa trang o nhap lieu
            newNameField.clear();
            newDescField.clear();
            newPriceField.clear();
            newDurationField.clear();
            categoryComboBox.setValue("General");

            // 4. Update lai bang thong so
            updateDashboardStats();

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
        Item selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            User loggedInUser = SessionManager.getInstance().getCurrentUser();

            // 3. KIEM TRA QUYEN SO HUU - dung ID de check chinh xac
            boolean isOwner = loggedInUser != null
                    && (loggedInUser.getId() == selectedProduct.getSellerId()
                    || "Admin".equals(loggedInUser.getRole()));

            if (isOwner) {
                // Chinh chu hoac Admin -> Cho phep xoa
                productList.remove(selectedProduct);
                DataManager.deleteProduct(selectedProduct);
                updateDashboardStats();

            } else {
                // Khong phai chinh chu -> Tu choi
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.setTitle("Lỗi phân quyền");
                alert.setHeaderText(null);

                javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(8);
                header.setStyle("-fx-background-color: #DC2626; -fx-padding: 22 24 18 24; -fx-alignment: CENTER;");
                javafx.scene.control.Label icon = new javafx.scene.control.Label("🚫");
                icon.setStyle("-fx-font-size: 30px;");
                javafx.scene.control.Label titleLbl = new javafx.scene.control.Label("Hành động bị từ chối!");
                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
                header.getChildren().addAll(icon, titleLbl);

                javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(0);
                body.setStyle("-fx-background-color: white; -fx-padding: 18 24 12 24; -fx-alignment: CENTER;");
                javafx.scene.control.Label msg = new javafx.scene.control.Label(
                        "Bạn không thể xóa sản phẩm của người khác.\nChỉ chủ sở hữu hoặc Admin mới có quyền xóa.");
                msg.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #374151; -fx-wrap-text: true; -fx-text-alignment: center;");
                msg.setWrapText(true);
                body.getChildren().add(msg);

                javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
                root.setStyle("-fx-background-color: white;");
                root.setPrefWidth(340);
                root.getChildren().addAll(header, body);

                alert.getDialogPane().setContent(root);
                alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;");

                javafx.scene.control.ButtonType btnOk = new javafx.scene.control.ButtonType(
                        "Đã hiểu", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(btnOk);
                alert.setOnShown(ev -> {
                    javafx.scene.Node btn = alert.getDialogPane().lookupButton(btnOk);
                    if (btn != null) btn.setStyle(
                            "-fx-background-color: #DC2626; -fx-text-fill: white;" +
                                    "-fx-font-size: 13px; -fx-font-weight: bold;" +
                                    "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 9 24;");
                });
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
    private void openBidWindow(Item product) {
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
                DataManager.updateProduct(product);
                updateDashboardStats();
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
            Item highestPriceProduct = productList.get(0);
            Item mostBidsProduct = productList.get(0);

            for (Item p : productList) {
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

    public void refreshTable(List<Item> newList) {
        Platform.runLater(() -> {
            if (productList == null) {
                productList = FXCollections.observableArrayList();
                productTable.setItems(productList);
            }
            productList.setAll(newList);
            updateDashboardStats();
        });
    }

    // ==========================================
    // SIDEBAR NAVIGATION
    // ==========================================

    /** Đặt item được chọn thành active, reset các item còn lại */
    private void setActiveNav(HBox activeItem) {
        HBox[] allItems = {navDashboard, navProducts, navCalendar,
                navNotifications, navSettings, navHelp};
        for (HBox item : allItems) {
            if (item == null) continue;
            if (item == activeItem) {
                item.getStyleClass().setAll("sidebar-item-active");
            } else {
                item.getStyleClass().setAll("sidebar-item");
            }
        }
    }

    @FXML
    public void handleNavDashboard(javafx.scene.input.MouseEvent event) {
        setActiveNav(navDashboard);
        // Dashboard luôn hiển thị sẵn — chỉ refresh lại data
        loadDataFromDatabase();
    }

    @FXML
    public void handleNavProducts(javafx.scene.input.MouseEvent event) {
        setActiveNav(navProducts);
        // Hiển thị danh sách sản phẩm (dùng lại bảng hiện tại)
        loadDataFromDatabase();
        showInfoPopup("Danh sách sản phẩm",
                "📦  Đang hiển thị toàn bộ sản phẩm đang đấu giá.\n\n" +
                        "Nhấn đúp vào một dòng để xem chi tiết và đặt giá.");
    }

    @FXML
    public void handleNavCalendar(javafx.scene.input.MouseEvent event) {
        setActiveNav(navCalendar);
        // Lọc và hiển thị các sản phẩm còn hạn đấu giá
        if (productList != null) {
            long active = productList.stream()
                    .filter(p -> p.getEndTime() != null &&
                            p.getEndTime().isAfter(java.time.LocalDateTime.now()))
                    .count();
            showInfoPopup("Lịch đấu giá",
                    "📅  Hiện có  " + active + "  phiên đấu giá đang diễn ra.\n\n" +
                            "Xem bảng bên phải để theo dõi thời gian kết thúc từng sản phẩm.");
        }
    }

    @FXML
    public void handleNavNotifications(javafx.scene.input.MouseEvent event) {
        setActiveNav(navNotifications);
        showInfoPopup("Thông báo",
                "🔔  Không có thông báo mới.\n\n" +
                        "Hệ thống sẽ thông báo khi có lượt đấu giá mới trên sản phẩm bạn quan tâm.");
    }

    @FXML
    public void handleNavSettings(javafx.scene.input.MouseEvent event) {
        setActiveNav(navSettings);
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String info = (currentUser != null)
                ? "👤  Tài khoản:  " + currentUser.getUsername() + "\n" +
                "🎭  Vai trò:      " + currentUser.getRole()
                : "Chưa đăng nhập";
        showInfoPopup("Cài đặt tài khoản", info +
                "\n\n⚙️  Các tùy chỉnh nâng cao sẽ được bổ sung trong phiên bản tiếp theo.");
    }

    @FXML
    public void handleNavHelp(javafx.scene.input.MouseEvent event) {
        setActiveNav(navHelp);
        showInfoPopup("Hỗ trợ & Hướng dẫn",
                "❓  Hướng dẫn sử dụng:\n\n" +
                        "•  Nhấn đúp vào sản phẩm trong bảng để xem chi tiết và đặt giá.\n" +
                        "•  Nhấn \"Xem biểu đồ\" để theo dõi lịch sử tăng giá.\n" +
                        "•  Seller có thể thêm sản phẩm qua thanh bên dưới bảng.\n\n" +
                        "📧  Liên hệ hỗ trợ: support@auctionuet.edu.vn");
    }

    /** Hiển thị popup thông tin nhỏ gọn, có style xanh lá */
    private void showInfoPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Header xanh
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(4);
        header.setStyle("-fx-background-color: #1A2E22; -fx-padding: 16 20;");
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        header.getChildren().add(titleLabel);

        // Body trắng
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox();
        body.setStyle("-fx-background-color: white; -fx-padding: 18 20 10 20;");
        javafx.scene.control.Label msgLabel = new javafx.scene.control.Label(message);
        msgLabel.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #374151; -fx-line-spacing: 4;");
        msgLabel.setWrapText(true);
        body.getChildren().add(msgLabel);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(400);
        root.getChildren().addAll(header, body);

        alert.getDialogPane().setContent(root);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;");

        ButtonType btnOk = new ButtonType("Đóng", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnOk);

        alert.setOnShown(ev -> {
            javafx.scene.Node okBtn = alert.getDialogPane().lookupButton(btnOk);
            if (okBtn != null) {
                okBtn.setStyle(
                        "-fx-background-color: #2D6A4F; -fx-text-fill: white;" +
                                "-fx-font-weight: bold; -fx-background-radius: 8;" +
                                "-fx-cursor: hand; -fx-padding: 8 20;");
            }
        });

        alert.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá UET - Đăng nhập");
    }

    // ==========================================
    // HÀM HIỂN THỊ CHI TIẾT SẢN PHẨM (Bidder)
    // ==========================================
    private void showBidderProductDetail(Item product) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Thông tin sản phẩm");
        alert.setHeaderText(null); // Tắt header mặc định để tự vẽ

        // ── Phần header xanh đậm ──────────────────────────────────────
        javafx.scene.layout.VBox headerSection = new javafx.scene.layout.VBox(6);
        headerSection.setStyle(
                "-fx-background-color: #1A2E22;" +
                        "-fx-padding: 20 24 20 24;"
        );

        javafx.scene.control.Label tagLabel = new javafx.scene.control.Label("🏷️  THÔNG TIN SẢN PHẨM");
        tagLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #52B788;"
        );

        javafx.scene.control.Label productNameLabel = new javafx.scene.control.Label(
                product.getName().toUpperCase()
        );
        productNameLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"
        );

        headerSection.getChildren().addAll(tagLabel, productNameLabel);

        // ── Phần thân trắng chứa các dòng thông tin ──────────────────
        javafx.scene.layout.VBox bodySection = new javafx.scene.layout.VBox(0);
        bodySection.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 18 24 8 24;"
        );

        // 1. Tạo một cái Label để thay thế vùng hiển thị mặc định
        javafx.scene.control.Label contentLabel = new javafx.scene.control.Label();
        contentLabel.setStyle(
                "-fx-font-size: 13.5px;" +
                        "-fx-text-fill: #374151;" +
                        "-fx-line-spacing: 5;" +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        contentLabel.setWrapText(true);
        contentLabel.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE); // Ép Label tự phình to theo chữ

        bodySection.getChildren().add(contentLabel);

        // ── Ghép header + body vào container tổng ────────────────────
        javafx.scene.layout.VBox dialogRoot = new javafx.scene.layout.VBox(0);
        dialogRoot.setStyle("-fx-background-color: white;");
        dialogRoot.setPrefWidth(460);
        dialogRoot.getChildren().addAll(headerSection, bodySection);

        alert.getDialogPane().setContent(dialogRoot);
        alert.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        // 2. Setup nút bấm (Đã thêm Xem biểu đồ)
        ButtonType btnClose    = new ButtonType("Đóng",          javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnViewChart = new ButtonType("Xem biểu đồ", javafx.scene.control.ButtonBar.ButtonData.APPLY);
        ButtonType btnBid      = new ButtonType("Ra giá ngay!",  javafx.scene.control.ButtonBar.ButtonData.OK_DONE);

        // Nạp cả 3 nút vào bảng
        alert.getButtonTypes().setAll(btnClose, btnViewChart, btnBid);

        // Style các nút sau khi dialog hiển thị
        alert.setOnShown(ev -> {
            // Nút "Ra giá ngay!" — xanh lá đậm
            javafx.scene.Node nodeBid = alert.getDialogPane().lookupButton(btnBid);
            if (nodeBid != null) {
                nodeBid.setStyle(
                        "-fx-background-color: #2D6A4F; -fx-text-fill: white;" +
                                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                                "-fx-background-radius: 8; -fx-cursor: hand;" +
                                "-fx-padding: 8 16;"
                );
            }
            // Nút "Xem biểu đồ" — xanh lá nhạt outline
            javafx.scene.Node nodeChart = alert.getDialogPane().lookupButton(btnViewChart);
            if (nodeChart != null) {
                nodeChart.setStyle(
                        "-fx-background-color: #D1FAE5; -fx-text-fill: #1A2E22;" +
                                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                                "-fx-background-radius: 8; -fx-cursor: hand;" +
                                "-fx-padding: 8 16;" +
                                "-fx-border-color: #6EE7B7; -fx-border-radius: 8;"
                );
            }
            // Nút "Đóng" — xám nhạt
            javafx.scene.Node nodeClose = alert.getDialogPane().lookupButton(btnClose);
            if (nodeClose != null) {
                nodeClose.setStyle(
                        "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280;" +
                                "-fx-font-size: 13px;" +
                                "-fx-background-radius: 8; -fx-cursor: hand;" +
                                "-fx-padding: 8 16;" +
                                "-fx-border-color: #E5E7EB; -fx-border-radius: 8;"
                );
            }
        });

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

            // [MỚI] Xử lý hiển thị Tên và Email
            String sellerName = (product.getSellerName() != null) ? product.getSellerName() : "Ẩn danh";
            String sellerContact = (product.getSellerEmail() != null) ? product.getSellerEmail() : "Không có";

            String content =
                    "👤  Người bán:       " + sellerName + "\n"
                            + "📧  Liên hệ:          " + sellerContact + "\n\n"
                            + "📝  Mô tả:             " + desc + "\n\n"
                            + "💵  Giá khởi điểm:  " + String.format("%,.0f VND", product.getStartPrice()).replace(",", ".") + "\n"
                            + "💰  Giá cao nhất:    " + String.format("%,.0f VND", product.getCurrentPrice()).replace(",", ".") + "\n\n"
                            + (isEnded
                            ? "🔴  Trạng thái:  Phiên đấu giá đã kết thúc!"
                            : "⏱   Còn lại:       " + timeRemaining);

            contentLabel.setText(content);

            javafx.scene.Node bidButton = alert.getDialogPane().lookupButton(btnBid);
            if (bidButton != null) {
                bidButton.setDisable(isEnded);
            }
        };

        // 4. GỌI LỆNH LẦN 1
        updateTimeLogic.run();

        // 5.Timeline
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    updateTimeLogic.run();
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

        alert.setOnHidden(e -> timeline.stop()); // Tắt bảng thì dừng đồng hồ
        alert.getDialogPane().setMinWidth(460);

        // 6. Hiển thị bảng và Xử lý sự kiện bấm nút
        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnBid) {
                openBidWindow(product); // Chuyển sang màn hình ra giá
            } else if (result.get() == btnViewChart) {
                showPriceHistoryChart(product); // Bật popup vẽ biểu đồ Line Chart lên
            }
        }
    }

    // ==========================================
    // HÀM VẼ BIỂU ĐỒ TỪ DATABASE BIDS
    // ==========================================
    private void showPriceHistoryChart(Item product) {
        Stage stage = new Stage();
        stage.setTitle("Lịch sử giá: " + product.getName());

        // ── Trục X ─────────────────────────────────────────────────────
        final javafx.scene.chart.NumberAxis xAxis = new javafx.scene.chart.NumberAxis();
        xAxis.setLabel("Lượt đấu giá (0 = Giá gốc)");
        xAxis.setMinorTickVisible(false);

        // ── Trục Y — format số gọn (K / M) ────────────────────────────
        final javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Giá (VND)");
        yAxis.setForceZeroInRange(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override public String toString(Number n) {
                double v = n.doubleValue();
                if (v >= 1_000_000_000) return String.format("%.1fB", v / 1_000_000_000);
                if (v >= 1_000_000)     return String.format("%.0fM", v / 1_000_000);
                if (v >= 1_000)         return String.format("%.0fK", v / 1_000);
                return String.format("%.0f", v);
            }
            @Override public Number fromString(String s) { return 0; }
        });

        // ── AreaChart (tên biến giữ nguyên: lineChart) ─────────────────
        final javafx.scene.chart.AreaChart<Number, Number> lineChart =
                new javafx.scene.chart.AreaChart<>(xAxis, yAxis);
        lineChart.setTitle(null);           // Title sẽ vẽ tay ở header
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true);
        lineChart.setLegendVisible(false);

        // ── Series ─────────────────────────────────────────────────────
        javafx.scene.chart.XYChart.Series<Number, Number> series =
                new javafx.scene.chart.XYChart.Series<>();
        series.setName("Diễn biến giá");

        // Điểm đầu = giá gốc
        series.getData().add(new javafx.scene.chart.XYChart.Data<>(0, product.getStartPrice()));

        // Theo dõi High / Low để hiện stats bar
        double[] statHigh = { product.getStartPrice() };
        double[] statLow  = { product.getStartPrice() };

        // ── Kéo dữ liệu từ DB ─────────────────────────────────────────
        String sql = "SELECT bid_price FROM bids WHERE product_id = ? ORDER BY bid_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getId());
            ResultSet rs = pstmt.executeQuery();

            int bidCounter = 1;
            while (rs.next()) {
                double price = rs.getDouble("bid_price");
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(bidCounter, price));
                if (price > statHigh[0]) statHigh[0] = price;
                if (price < statLow[0])  statLow[0]  = price;
                bidCounter++;
            }
        } catch (Exception e) {
            System.out.println(">> Lỗi tải biểu đồ: " + e.getMessage());
        }

        lineChart.getData().add(series);

        // ── Tính toán stats ────────────────────────────────────────────
        double currentPrice = product.getCurrentPrice() > 0
                ? product.getCurrentPrice() : product.getStartPrice();
        double startPrice   = product.getStartPrice();
        double changeAmt    = currentPrice - startPrice;
        double changePct    = startPrice > 0 ? (changeAmt / startPrice) * 100.0 : 0.0;
        boolean isUp        = changeAmt >= 0;

        String fmtCurrent = String.format("%,.0f VND", currentPrice).replace(",", ".");
        String fmtStart   = String.format("%,.0f VND", startPrice).replace(",", ".");
        String fmtHigh    = String.format("%,.0f VND", statHigh[0]).replace(",", ".");
        String fmtLow     = String.format("%,.0f VND", statLow[0]).replace(",", ".");
        String fmtChange  = String.format("%s%,.0f VND",
                isUp ? "+" : "", changeAmt).replace(",", ".");
        String fmtPct     = String.format("(%s%.1f%%)", isUp ? "+" : "", changePct);

        // ══════════════════════════════════════════════════════════════
        //  LAYOUT
        // ══════════════════════════════════════════════════════════════

        // 1. Dark header bar (top)
        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox();
        topBar.setStyle("-fx-background-color: #1A2E22; -fx-padding: 14 20; -fx-alignment: CENTER_LEFT;");
        javafx.scene.control.Label topBarTitle = new javafx.scene.control.Label(
                "📈   Lịch sử giá — " + product.getName());
        topBarTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        topBar.getChildren().add(topBarTitle);

        // 2. Price header
        javafx.scene.layout.VBox priceHeaderBox = new javafx.scene.layout.VBox(6);
        priceHeaderBox.setStyle("-fx-padding: 20 24 12 24; -fx-background-color: white;");

        javafx.scene.control.Label smallLabel = new javafx.scene.control.Label("Giá hiện tại (cao nhất)");
        smallLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        javafx.scene.layout.HBox priceRow = new javafx.scene.layout.HBox(12);
        priceRow.setStyle("-fx-alignment: CENTER_LEFT;");

        javafx.scene.control.Label priceLabel = new javafx.scene.control.Label(fmtCurrent);
        priceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1A2E22;");

        javafx.scene.control.Label pctBadge = new javafx.scene.control.Label(
                (isUp ? "▲ " : "▼ ") + fmtPct);
        pctBadge.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-color: " + (isUp ? "#D1FAE5" : "#FEE2E2") + ";" +
                        "-fx-text-fill: " + (isUp ? "#059669" : "#DC2626") + ";" +
                        "-fx-background-radius: 20; -fx-padding: 4 10;");

        javafx.scene.control.Label sinceLabel = new javafx.scene.control.Label("so với giá gốc");
        sinceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        priceRow.getChildren().addAll(priceLabel, pctBadge, sinceLabel);
        priceHeaderBox.getChildren().addAll(smallLabel, priceRow);

        // 3. Stats bar
        javafx.scene.layout.HBox statsBar = new javafx.scene.layout.HBox(0);
        statsBar.setStyle(
                "-fx-background-color: #F9FAFB; -fx-padding: 12 24;" +
                        "-fx-border-color: #E5E7EB; -fx-border-width: 1 0 1 0;");

        // Helper: tạo 1 ô stat (label + value)
        javafx.scene.layout.VBox cell1 = makeStatCell("Giá khởi điểm", fmtStart,  "#1F2937");
        javafx.scene.layout.VBox cell2 = makeStatCell("Cao nhất",       fmtHigh,   "#059669");
        javafx.scene.layout.VBox cell3 = makeStatCell("Thấp nhất",      fmtLow,    "#DC2626");
        javafx.scene.layout.VBox cell4 = makeStatCell("Thay đổi",       fmtChange, isUp ? "#059669" : "#DC2626");

        // Dividers giữa các cell
        for (javafx.scene.layout.VBox cell : new javafx.scene.layout.VBox[]{cell1, cell2, cell3, cell4}) {
            cell.setPrefWidth(175);
            cell.setStyle("-fx-padding: 0 20 0 0;");
        }
        statsBar.getChildren().addAll(cell1, makeDivider(), cell2, makeDivider(), cell3, makeDivider(), cell4);

        // 4. Chart
        javafx.scene.layout.VBox.setVgrow(lineChart, javafx.scene.layout.Priority.ALWAYS);

        // 5. Assemble
        javafx.scene.layout.VBox chartContainer = new javafx.scene.layout.VBox(0);
        chartContainer.setStyle("-fx-background-color: white;");
        chartContainer.getChildren().addAll(topBar, priceHeaderBox, statsBar, lineChart);

        Scene scene = new Scene(chartContainer, 740, 540);

        try {
            String css = getClass().getResource("/com/uet/auction/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println(">> Không load được styles.css: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.setMinWidth(620);
        stage.setMinHeight(480);

        // Gắn Tooltip vào từng điểm sau khi chart đã render
        stage.setOnShown(ev -> {
            for (javafx.scene.chart.XYChart.Data<Number, Number> d : series.getData()) {
                if (d.getNode() == null) continue;
                String tipText = "Lượt:  " + d.getXValue() + "\n" +
                        "Giá:    " + String.format("%,.0f VND",
                        d.getYValue().doubleValue()).replace(",", ".");
                javafx.scene.control.Tooltip tip = new javafx.scene.control.Tooltip(tipText);
                tip.setStyle(
                        "-fx-font-size: 12px; -fx-font-family: 'Segoe UI';" +
                                "-fx-background-color: white; -fx-text-fill: #1A2E22;" +
                                "-fx-background-radius: 8; -fx-padding: 8 12;" +
                                "-fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.15),8,0,0,2);");
                javafx.scene.control.Tooltip.install(d.getNode(), tip);
            }
        });

        stage.show();
    }

    /** Tạo một ô thống kê nhỏ (tiêu đề + giá trị có màu) */
    private javafx.scene.layout.VBox makeStatCell(String label, String value, String valueColor) {
        javafx.scene.layout.VBox cell = new javafx.scene.layout.VBox(3);
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
        javafx.scene.control.Label val = new javafx.scene.control.Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");
        cell.getChildren().addAll(lbl, val);
        return cell;
    }

    /** Tạo đường phân cách dọc giữa các stat cell */
    private javafx.scene.layout.Region makeDivider() {
        javafx.scene.layout.Region div = new javafx.scene.layout.Region();
        div.setStyle("-fx-background-color: #E5E7EB; -fx-min-width: 1; -fx-pref-width: 1; -fx-margin: 0 20;");
        div.setPrefHeight(36);
        div.setMinWidth(1);
        div.setMaxWidth(1);
        javafx.scene.layout.HBox.setMargin(div, new javafx.geometry.Insets(0, 20, 0, 0));
        return div;
    }
}