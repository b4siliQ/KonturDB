package com.monolatte.kontur.model.Notes.Properties;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.SQL.Components_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.FloatProperty;

import java.util.ArrayList;
import java.util.List;

public class ProjectProperty {
    private final StringProperty name;
    private final StringProperty startDate;
    private final StringProperty endDate;
    private final StringProperty status;
    private final LongProperty id;
    private final IntegerProperty componentsQuantity;
    private final FloatProperty totalComponentPrice;

    private final Components_usageDAO _componentUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private List<Component> _componentList = new ArrayList<>();

    public ProjectProperty(Project project) {
        this._componentList.addAll(this._componentUsageDAO.getComponentsByProjectId(project.getId()));
        this.name = new SimpleStringProperty(project.getProject_name());
        this.startDate = new SimpleStringProperty(project.getStart_date());
        this.endDate = new SimpleStringProperty(project.getEnd_date());
        this.status = new SimpleStringProperty(project.getStatus());
        this.id = new SimpleLongProperty(project.getId());
        this.componentsQuantity = new SimpleIntegerProperty(this._componentQuantityCalculator());
        this.totalComponentPrice = new SimpleFloatProperty(this._totalComponentPriceCalculator());
    }

    @Override
    public String toString() { return this.name.get(); }

    public LongProperty idProperty() { return this.id; }
    public StringProperty nameProperty() { return this.name; }
    public StringProperty startDateProperty() { return this.startDate; }
    public StringProperty endDateProperty() { return this.endDate; }
    public StringProperty statusProperty() { return this.status; }
    public IntegerProperty componentsQuantityProperty() { return this.componentsQuantity; }
    public FloatProperty totalComponentPriceProperty() { return this.totalComponentPrice; }

    public List<Component> getComponentList() { return this._componentList; }

    private int _componentQuantityCalculator() {
        int result = 0;
        for (var component : this._componentList) {
            result += component.getQuantity();
        }
        return result;
    }

    private float _totalComponentPriceCalculator() {
        float result = 0;
        for (var component : this._componentList) {
            result += component.getQuantity() * component.getPrice();
        }
        return result;
    }
}