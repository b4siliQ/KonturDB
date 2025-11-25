package com.monolatte.kontur.model.Notes;

public class Component_usage {
    long id;
    long project_id;
    long component_id;
    int quantity;

    public Component_usage(long project_id, long component_id, int quantity) {
        this.project_id = project_id;
        this.component_id = component_id;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Component_usage [id=" + id + "]";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProject_id() {
        return project_id;
    }

    public void setProject_id(long project_id) {
        this.project_id = project_id;
    }

    public long getComponent_id() {
        return component_id;
    }

    public void setComponent_id(long component_id) {
        this.component_id = component_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
