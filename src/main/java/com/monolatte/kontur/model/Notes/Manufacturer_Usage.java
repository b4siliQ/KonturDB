package com.monolatte.kontur.model.Notes;

public class Manufacturer_Usage {
    long id;
    long component_id;
    long manufacturer_id;

    public Manufacturer_Usage(long id, long component_id, long manufacturer_id) {
        this.id = id;
        this.component_id = component_id;
        this.manufacturer_id = manufacturer_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getComponent_id() {
        return component_id;
    }

    public void setComponent_id(long component_id) {
        this.component_id = component_id;
    }

    public long getManufacturer_id() {
        return manufacturer_id;
    }

    public void setManufacturer_id(long manufacturer_id) {
        this.manufacturer_id = manufacturer_id;
    }
}
