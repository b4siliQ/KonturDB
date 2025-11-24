package com.monolatte.kontur.model.SQLManager.Notes;

public class Component_usage {
    int id;
    int project_id;
    int component_id;
    int quantity;

    public Component_usage(int id, int project_id, int component_id, int quantity) {
        this.id = id;
        this.project_id = project_id;
        this.component_id = component_id;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int getComponent_id() {
        return component_id;
    }

    public void setComponent_id(int component_id) {
        this.component_id = component_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
