package com.monolatte.kontur.model.Notes.Properties;

import com.monolatte.kontur.model.Notes.Project;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProjectProperty {
    private final StringProperty name;
    private final StringProperty startDate;
    private final StringProperty endDate;
    private final StringProperty status;
    private final LongProperty id;

    public ProjectProperty(Project project) {
        this.name = new SimpleStringProperty(project.getProject_name());
        this.startDate = new SimpleStringProperty(project.getStart_date());
        this.endDate = new SimpleStringProperty(project.getEnd_date());
        this.status = new SimpleStringProperty(project.getStatus());
        this.id = new SimpleLongProperty(project.getId());
    }

    @Override
    public String toString() { return this.name.get(); }

    public LongProperty idProperty() { return this.id; }
    public StringProperty nameProperty() { return this.name; }
    public StringProperty startDateProperty() { return this.startDate; }
    public StringProperty endDateProperty() { return this.endDate; }
    public StringProperty statusProperty() { return this.status; }
}