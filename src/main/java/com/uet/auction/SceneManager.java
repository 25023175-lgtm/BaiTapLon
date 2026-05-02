package com.uet.auction;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;

    // Thiết lập Stage chính (gọi một lần duy nhất lúc khởi động)
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    // Hàm dùng chung để chuyển màn hình
    public static void switchScene(String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
            // Kích thước mặc định là 400x500 cho các màn hình khởi đầu
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Lỗi nạp file FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}