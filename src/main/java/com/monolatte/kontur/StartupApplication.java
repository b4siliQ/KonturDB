package com.monolatte.kontur;

import com.monolatte.kontur.model.SQL.ISQLDAOBase;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartupApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        this._initTables();

        FXMLLoader fxmlLoader = new FXMLLoader(StartupApplication.class.getResource("MainPanel.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage.setTitle("Kontur");
        stage.setScene(scene);
        stage.show();
    }

    private void _initTables() {
        SQLTableManager.getInstance().getAllManagers()
                .forEach(ISQLDAOBase::createTable);
    }

    public static void main(String[] args) {
        launch();
    }
}