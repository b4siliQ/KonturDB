package com.monolatte.kontur.model.Notes;

public class Component_usage extends BaseNote {
    long project_id;
    long component_id;

    public Component_usage(long project_id, long component_id) {
        this.project_id = project_id;
        this.component_id = component_id;
    }

    @Override
    public String toString() {
        return "Component_usage [id=" + id + "]";
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
}
