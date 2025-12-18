package com.monolatte.kontur.model.SQL;

public class DAOFactory {

    public enum DAOType {
        COMPONENT,
        PROJECT,
        MANUFACTURER,
        USER
    }

    public static ISQLDAO<?> createDAO(DAOType type) {
        switch (type) {
            case COMPONENT -> {
                return SQLTableManager.getInstance().getComponentsManager();
            }
            case PROJECT -> {
                return SQLTableManager.getInstance().getProjectManager();
            }
            case MANUFACTURER -> {
                return SQLTableManager.getInstance().getManufacturerDAO();
            }
            case USER -> {
                return SQLTableManager.getInstance().getUserDAO();
            }
            default -> throw new IllegalArgumentException(String.format("Unknown DAO type %s", type));
        }
    }
}
