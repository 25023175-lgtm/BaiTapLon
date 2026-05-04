package com.uet.auction;

import javafx.application.Application;
import javafx.stage.Stage;
import atlantafx.base.theme.PrimerLight; // Thêm dòng import này ở trên cùng



public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        // 1. Giao Stage cho Manager quản lý
        SceneManager.setStage(stage);

        // 2. Dùng Manager để mở màn hình đầu tiên
        SceneManager.switchScene("login-view.fxml", "Hệ thống Đấu giá - Đăng nhập");
    }

    public static void main(String[] args) {
        launch();
    }
}