module com.uet.auction {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires java.desktop;
    requires atlantafx.base;

    opens com.uet.auction to javafx.fxml;
    exports com.uet.auction;
    exports com.auction.model;
    opens com.auction.model to javafx.base;
}