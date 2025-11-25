package com.monolatte.kontur.model.Notes;

public class Project {
    private int id;
    private String project_name;
    private String start_date;
    private String end_date;
    private String status;

    public Project(int id, String project_name, String start_date, String end_date, String status) {
        this.id = id;
        this.project_name = project_name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.status = status;
    }

    @Override
    public String toString() {
        return this.project_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
