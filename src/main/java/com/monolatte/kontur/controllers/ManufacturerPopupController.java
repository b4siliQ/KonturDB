package com.monolatte.kontur.controllers;

import com.monolatte.kontur.controllers.subs.ProjectTableColumn;
import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.model.Notes.Enums.ManufacturerColumns;
import com.monolatte.kontur.model.Notes.Properties.ComponentProperty;
import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
import com.monolatte.kontur.model.SQL.*;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ManufacturerPopupController {
    @FXML
    TextArea descriptionTextArea;
    @FXML
    TextField nameTextField;
    @FXML
    TextField cityTextField;
    @FXML
    TextField addressTypeTextField;
    @FXML
    Button showManufacturerButton;
    @FXML
    TableView<ComponentProperty> componentsTable;
    @FXML
    TableColumn<ComponentProperty, Long> idTableColumn;
    @FXML
    TableColumn<ComponentProperty, String> nameTableColumn;
    @FXML
    TableColumn<ComponentProperty, String> typeTableColumn;
    @FXML
    TableColumn<ComponentProperty, Float> priceTableColumn;
    @FXML
    TableColumn<ComponentProperty, Integer> quantityTableColumn;
    @FXML
    TableColumn<ComponentProperty, Float> finalCostTableColumn;
    @FXML
    TableColumn<ComponentProperty, Void> projectsTableColumn;

    private final ComponentsDAO _componentsDAO = SQLTableManager.getInstance().getComponentsManager();
    private final ManufacturerAdressesDAO _manufacturerAddressDAO = SQLTableManager.getInstance().getManufacturerAddressDAO();
    private final Manufacturer_usageDAO _manufacturerUsageDAO = SQLTableManager.getInstance().getManufacturerUsageDAO();
    private final ObservableList<ComponentProperty> _masterData = FXCollections.observableArrayList();
    private Manufacturer _currentManufacturer;
    private ManufacturerAddresses _currentManufacturerAddress;
    //private Component _currentComponent;


    @FXML
    public void initialize() {
        this._setupTableColumns();
    }

    @FXML
    public void onShowManufacturerButtonClicked() {
        try {
            FXMLLoader popupLoader = new FXMLLoader(ManufacturerPopupController.class.getResource(
                    "/com/monolatte/kontur/SearchPopup.fxml"
            ));
            Parent root = popupLoader.load();
            SearchPopupController<Manufacturer> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.MANUFACTURER);
            popupController.initData(ManufacturerColumns.values());

            var newStage = new Stage();
            var newScene = new Scene(root);
            newStage.setScene(newScene);
            popupController.setStage(newStage);

            newStage.setTitle("Manufacturer Searcher");
            newStage.setResizable(false);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._currentManufacturer = result;
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        this._currentManufacturerAddress = this._manufacturerAddressDAO.getManufacturerAddressesByManufacturerId(
                this._currentManufacturer.getId()
        );

        this._loadDataIntoTable();
        this._refillManufacturerFields();
    }

    private void _refillManufacturerFields() {
        this.nameTextField.setText(this._currentManufacturer.getName());
        this.descriptionTextArea.setText(this._currentManufacturer.getDescription());
        this.addressTypeTextField.setText(this._currentManufacturerAddress.getAddresses_type());
        this.cityTextField.setText(this._currentManufacturerAddress.getCity());
    }

    private void _setupTableColumns() {
        this.idTableColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        this.nameTableColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.typeTableColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        this.priceTableColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        this.quantityTableColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        this.priceTableColumn.setCellValueFactory(cellData -> {
            ComponentProperty component = cellData.getValue();
            float result = component.priceProperty().getValue() * component.quantityProperty().getValue();
            return new SimpleFloatProperty(result).asObject();
        });

        this.finalCostTableColumn.setCellValueFactory(cellData -> {
            ComponentProperty component = cellData.getValue();
            float result = component.priceProperty().get() * component.quantityProperty().get();

            return new SimpleFloatProperty(result).asObject();
        });

        this.projectsTableColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Проекты");

            {
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(event -> {
                    // Извлекаем объект ComponentProperty из текущей строки
                    ComponentProperty item = getTableRow().getItem();
                    if (item != null) {
                        System.out.println("Выбран компонент: " + item.idProperty().get());
                        // Здесь вызывайте метод для открытия проектов, передавая в него 'item'
                        try {
                            var popupLoader = new FXMLLoader(ManufacturerPopupController.class.getResource(
                                    "/com/monolatte/kontur/subs/ProjectTable.fxml"
                            ));
                            Parent root = popupLoader.load();
                            ProjectTableColumn popupController = popupLoader.getController();

                            popupController.setId(item.idProperty().get());

                            var popupStage = new Stage();
                            var popupScene = new Scene(root);
                            popupStage.setScene(popupScene);

                            popupStage.setTitle("Current ID Project Table");
                            popupStage.setResizable(false);
                            popupStage.initModality(Modality.APPLICATION_MODAL);
                            popupStage.showAndWait();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void _loadDataIntoTable() {
        this._fillComponentPropertyList(this._componentsDAO.getAllNotes());
        this.componentsTable.setItems(this._masterData);
    }

    private void _fillComponentPropertyList(List<Component> componentList) {
        this._masterData.clear();
        for (var component : componentList) {
            this._masterData.add(new ComponentProperty(component));
        }
    }
}
