package com.monolatte.kontur.model.Notes;

public class Manufacturer extends BaseNote {
    private long component_id;
    private String name;
    private String description;

    public Manufacturer(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public long getComponent_id() {
        return component_id;
    }

    public void setComponent_id(long component_id) {
        this.component_id = component_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
