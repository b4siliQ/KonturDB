package com.monolatte.kontur.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;

import java.io.IOException;

public class MainPanelController {
    @FXML
    Tab componentTab;
    @FXML
    Tab projectTab;

    private ComponentsPanelController _componentPanelController;

    @FXML
    public void initialize() {
        try {
            FXMLLoader componentLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/ComponentsPanel.fxml"
            ));
            Parent subview = componentLoader.load();
            this.componentTab.setContent(subview);
            this._componentPanelController = componentLoader.getController();
        } catch (IOException e) {
            System.err.println("Возникла проблема при инициализации вкладки компонентов");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader projectsLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/ProjectsPanel.fxml"
            ));
            Parent subview = projectsLoader.load();
            this.projectTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Возникла проблема при инициализации вкладки проектов");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
