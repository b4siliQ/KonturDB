package com.monolatte.kontur.model.Notes.Enums;

public enum UserColumns implements IColumn {
    USER_NAME(1, "user_name"),
    DESCRIPTION(2, "end_d");

    private final int _code;
    private final String _description;

    UserColumns(int code, String description) {
        this._code = code;
        this._description = description;
    }

    @Override
    public String toString() {
        return this._description;
    }

    @Override
    public String getDescription() { return this._description; }

    public static UserColumns getByDescription(String description) {
        for (var column : UserColumns.values()) {
            if (column.getDescription().equalsIgnoreCase(description)) {
                return column;
            }
        }
        throw new IllegalArgumentException(String.format("Alert! project column with description %s not founded",
                description
        ));
    }
}
