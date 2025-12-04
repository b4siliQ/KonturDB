package com.monolatte.kontur.model.Notes.Enums;

public enum ComponentsType implements IColumn {
    CONTROLLER(1, "Controller"),
    DIODE(2, "Diode"),
    RESISTOR(3, "Resistor"),
    TRANSISTOR(4, "Transistor"),
    CUSTOM(5, "Custom"),
    EMPTY(6, "Empty");

    private final int _code;
    private final String _description;

    ComponentsType(int code, String description) {
        this._code = code;
        this._description = description;
    }

    @Override
    public String getDescription() { return this._description; }

    public static ComponentsType getByDescription(String description) {
        for (var column : ComponentsType.values()) {
            if (column.getDescription().equalsIgnoreCase(description)) {
                return column;
            }
        }
        throw new IllegalArgumentException(String.format("Alert! project column with description %s not founded",
                description
        ));
    }

    @Override
    public String toString() {
        return this._description;
    }
}
