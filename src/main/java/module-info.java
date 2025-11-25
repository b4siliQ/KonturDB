module com.monolatte.kontur {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;


    opens com.monolatte.kontur.controllers to javafx.fxml;
    exports com.monolatte.kontur;
}