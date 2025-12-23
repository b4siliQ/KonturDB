package com.monolatte.kontur.model.Notes.Properties;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.Notes.User;
import com.monolatte.kontur.model.SQL.Components_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class ManufacturerProperty {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty description;

    private final Components_usageDAO _componentUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private List<Project> _projectList = new ArrayList<>();

    public ManufacturerProperty(LongProperty id, User user) {
        this.id = new SimpleLongProperty(user.getId());
        this.name = new SimpleStringProperty(user.getName());
        this.description = new SimpleStringProperty(user.getDescription());
    }

    @Override
    public String toString() { return this.name.get(); }

    public LongProperty idProperty() { return this.id; }
    public StringProperty nameProperty() { return this.name; }
    public StringProperty descriptionProperty() { return this.description; }
}

