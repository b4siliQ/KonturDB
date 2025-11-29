package com.monolatte.kontur.model.Notes;

import javafx.beans.property.*;

public class ComponentProperty {

    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty specification;
    private final StringProperty datasheetLink; // Изменено для лучшего соответствия JavaFX стилю
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
    public String toString() { return this.name.get(); } // Получаем значение из Property

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public LongProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty specificationProperty() {
        return specification;
    }

    // Важно: соответствует полю datasheetLink
    public StringProperty datasheetLinkProperty() {
        return datasheetLink;
    }

    public FloatProperty priceProperty() {
        return price;
    }

    // Геттеры/Сеттеры для ID (должны быть в BaseNote, но добавим сюда для примера)
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
}
