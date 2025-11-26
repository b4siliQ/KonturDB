package com.monolatte.kontur.model.Notes.Enums;

public enum ProjectStatus {
    DRAFT(1, "Draft"),
    INPROGRESS(2, "In Progress"),
    COMPLETED(3, "Completed"),
    CANCELED(4, "Canceled");

    private final int _code;
    private final String _description;

    ProjectStatus(int code, String description) {
        this._code = code;
        this._description = description;
    }

    public String getDescription() {
        return this._description;
    }

    public static ProjectStatus getByDescription(String description) {
        for (var status : values()) {
            if (status.getDescription().equalsIgnoreCase(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException(String.format("Alert! Status with description %s not founded", description));
    }
}
