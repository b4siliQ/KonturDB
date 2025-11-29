package com.monolatte.kontur.model.Notes.Properties;

import com.monolatte.kontur.model.Notes.Component;
import javafx.beans.property.LongProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;

public class ComponentProperty {

    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty specification;
    private final StringProperty datasheetLink;
    private final FloatProperty price;
    private final LongProperty id;

    public ComponentProperty(Component component) {
        this.name = new SimpleStringProperty(component.getName());
        this.type = new SimpleStringProperty(component.getType());
        this.specification = new SimpleStringProperty(component.getSpecification());
        this.datasheetLink = new SimpleStringProperty(component.getDatasheet_link());
        this.price = new SimpleFloatProperty(component.getPrice());
        this.id = new SimpleLongProperty(component.getId());
    }

    @Override
    public String toString() { return this.name.get(); }

    public LongProperty idProperty() { return this.id; }
    public StringProperty nameProperty() { return this.name; }
    public StringProperty typeProperty() { return this.type; }
    public StringProperty specificationProperty() { return this.specification; }
    public StringProperty datasheetLinkProperty() { return this.datasheetLink; }
    public FloatProperty priceProperty() { return this.price; }
}