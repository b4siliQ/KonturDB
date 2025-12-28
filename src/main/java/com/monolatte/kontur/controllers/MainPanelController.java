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
    @FXML
    Tab manufacturerTab;
    @FXML
    Tab userTab;
    @FXML
    Tab aboutDevsTab;
    @FXML
    Tab projectsPolygonTab;
    @FXML
    Tab componentsPolygonTab;

    @FXML
    public void initialize() {
        try {
            FXMLLoader componentLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/ComponentsPanel.fxml"
            ));
            Parent subview = componentLoader.load();
            this.componentTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading component tab");
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader projectsLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/ProjectsPanel.fxml"
            ));
            Parent subview = projectsLoader.load();
            String cssPath = getClass().getResource("/com/monolatte/style/seregaStyle.css").toExternalForm();
            subview.getStylesheets().add(cssPath);
            this.projectTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading project tab");
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader manufacturerLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/ManufacturersPanel.fxml"
            ));
            Parent subview = manufacturerLoader.load();
            this.manufacturerTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading manufacturer tab");
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader userLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/UsersPanel.fxml"
            ));
            Parent subview = userLoader.load();
            String cssPath = getClass().getResource("/com/monolatte/style/seregaStyle.css").toExternalForm();
            subview.getStylesheets().add(cssPath);
            this.userTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading user tab");
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader aboutDevsLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/AboutDevelopersPanel.fxml"
            ));
            Parent subview = aboutDevsLoader.load();
            this.aboutDevsTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading user tab");
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader aboutpolygonLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/PolygonPanel.fxml"
            ));
            Parent subview = aboutpolygonLoader.load();
            String cssPath = getClass().getResource("/com/monolatte/style/seregaStyle.css").toExternalForm();
            subview.getStylesheets().add(cssPath);
            this.projectsPolygonTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading user tab");
            throw new RuntimeException(e);
        }

        try {
            FXMLLoader aboutPolygonControllerLoader = new FXMLLoader(MainPanelController.class.getResource(
                    "/com/monolatte/kontur/PolygonPanelComponents.fxml"
            ));
            Parent subview = aboutPolygonControllerLoader.load();
            this.componentsPolygonTab.setContent(subview);
        } catch (IOException e) {
            System.err.println("Alert! An error has occurred while loading user tab");
            throw new RuntimeException(e);
        }
    }
}
