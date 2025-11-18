module com.monolatte.kontur {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens com.monolatte.kontur.view to javafx.fxml;
    exports com.monolatte.kontur;
}