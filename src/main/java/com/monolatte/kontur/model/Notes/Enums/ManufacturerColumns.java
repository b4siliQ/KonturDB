package com.monolatte.kontur.model.Notes.Enums;

public enum ManufacturerColumns implements IColumn {
    NAME(1, "name"),
    DESCRIPTION(2, "description");

    private final int _code;
    private final String _description;

    ManufacturerColumns(int code, String description) {
        this._code = code;
        this._description = description;
    }

    @Override
    public String toString() { return this._description; }

    public static ManufacturerColumns getByDescription(String description) {
        for (var status : values()) {
            if (status.getDescription().equalsIgnoreCase(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException(String.format("Alert! Status with description %s not founded", description));
    }

    @Override
    public String getDescription() { return this._description; }
}
