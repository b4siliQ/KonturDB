package com.monolatte.kontur.model.Notes.Enums;

public enum ProjectColumns implements IColumn {
    PROJECT_NAME(1, "project_name"),
    START_DATE(2, "start_date"),
    END_DATE(3, "end_date");

    private final int _code;
    private final String _description;

    ProjectColumns(int code, String description) {
        this._code = code;
        this._description = description;
    }

    @Override
    public String toString() {
        return this._description;
    }

    @Override
    public String getDescription() { return this._description; }

    public static ProjectColumns getByDescription(String description) {
        for (var column : ProjectColumns.values()) {
            if (column.getDescription().equalsIgnoreCase(description)) {
                return column;
            }
        }
        throw new IllegalArgumentException(String.format("Alert! project column with description %s not founded",
                description
        ));
    }
}
