package com.monolatte.kontur.model.Notes.Enums;

public enum ComponentColumns implements IColumn {
    NAME(1, "name"),
    TYPE(2, "type");

    private final int _code;
    private final String _description;

    ComponentColumns(int code, String description) {
        this._code = code;
        this._description = description;
    }

    @Override
    public String getDescription() {
        return this._description;
    }

    public static ComponentColumns getByDescription(String description) {
        for (var column : ComponentColumns.values()) {
            if (column.getDescription().equalsIgnoreCase(description)) {
                return column;
            }
        }
        throw new IllegalArgumentException(String.format("Alert! Component column with description %s not founded",
                description
        ));
    }
}
