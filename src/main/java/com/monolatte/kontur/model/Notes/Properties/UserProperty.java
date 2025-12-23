package com.monolatte.kontur.model.Notes.Properties;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.User;
import com.monolatte.kontur.model.SQL.Components_usageDAO;
import com.monolatte.kontur.model.SQL.UserContactDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

public class UserProperty {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty description;

    private final Components_usageDAO _componentUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private List<Component> _componentList = new ArrayList<>();

    public UserProperty(LongProperty id, User user) {
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
