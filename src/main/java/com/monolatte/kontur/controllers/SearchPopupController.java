package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.BaseNote;
import com.monolatte.kontur.model.Notes.Enums.IColumn;
import com.monolatte.kontur.model.SQL.DAOFactory;
import com.monolatte.kontur.model.SQL.ISQLDAOSearchable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SearchPopupController<T extends BaseNote> {
    @FXML
    ListView<T> resultListView;
    @FXML
    ChoiceBox<IColumn> columnSorterChoiceBox;
    @FXML
    Button findButton;
    @FXML
    Button resetSearchButton;
    @FXML
    Button addButton;
    @FXML
    Button cancelButton;
    @FXML
    TextField searchTextField;

    private ISQLDAOSearchable<T> _DAO;
    private T _chosenObject;
    private Stage _stage;

    @FXML
    public void initialize() {

    }

    @FXML
    public void onResultListViewClicked() {

    }

    @FXML
    public void onFindButtonClicked() {
        var foundedObjects = FXCollections.observableList(this._DAO.search(
                this.columnSorterChoiceBox.getValue().getDescription(),
                this.searchTextField.getText()
        ));
        this.resultListView.setItems(foundedObjects);
    }

    @FXML
    public void onResetSearchButtonClicked() {
        this.searchTextField.setText("");
        this._refreshList();
    }

    @FXML
    public void onAddButtonClicked() {
        this._chosenObject = this.resultListView.getSelectionModel().getSelectedItem();
        if (this._stage != null) {
            this._stage.close();
        }
    }

    @FXML
    public void onCancelButtonClicked() {
        if (this._stage != null) {
            this._stage.close();
        }
    }

    public void initDAO(DAOFactory.DAOType type) {
        @SuppressWarnings("unchecked")
        ISQLDAOSearchable<T> dao = (ISQLDAOSearchable<T>) DAOFactory.createDAO(type);
        this._DAO = dao;
    }

    public void setStage(Stage stage) {
        this._stage = stage;
    }

    public void initData(IColumn[] columns) {
        this._refreshList();
        for (var column : columns) {
            this.columnSorterChoiceBox.getItems().add(column);
        }
    }

    public T getChosenObject() {
        return this._chosenObject;
    }

    private ObservableList<T> _updateList() {
        return FXCollections.observableList(this._DAO.getAllNotes());
    }

    private void _refreshList() {
        var update = this._updateList();
        this.resultListView.setItems(update);
    }
}
