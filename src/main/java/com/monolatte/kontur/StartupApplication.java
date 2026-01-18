package com.monolatte.kontur;

import com.monolatte.kontur.service.SQL.Interfaces.ISQLDAOBase;
import com.monolatte.kontur.service.SQL.SQLTableManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Инициализирующий класс. Служит для запуска приложения.
 *
 * @author b4siliQ (Basil Makes Games)
 * */
public class StartupApplication extends Application {
    /**
     * Метод, инициализирующий стартовое окно и задающий параметры для запуска.
     * */
    @Override
    public void start(Stage stage) throws IOException {
        this._initTables();

        String cssPath = StartupApplication.class.getResource("/com/monolatte/kontur/CSS/Mineral Blue.css")
                .toExternalForm();

        var fxmlLoader = new FXMLLoader(StartupApplication.class.getResource(
                "/com/monolatte/kontur/FXML/ComponentsPanel.fxml"
        ));

        Parent root = fxmlLoader.load();

        var scene = new Scene(root);
        root.getStylesheets().add(cssPath);
        stage.setTitle("KonturDB");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Метод, инициализирующий DAO компоненты для работы с таблицами в базе данных.
     * Используется только при запуске приложения.
     * */
    private void _initTables() {
        SQLTableManager.getInstance().getAllManagers()
                .forEach(ISQLDAOBase::createTable);
    }

    public static void main(String[] args) {
        launch();
    }
}