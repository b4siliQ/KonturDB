package com.monolatte.kontur.model.SQL;

public class DAOFactory {

    public enum DAOType {
        COMPONENT,
        PROJECT
    }

    public static ISQLDAO<?> createDAO(DAOType type) {
        switch (type) {
            case COMPONENT -> {
                return SQLTableManager.getInstance().getComponentsManager();
            }
            case PROJECT -> {
                return SQLTableManager.getInstance().getProjectManager();
            }
            default -> throw new IllegalArgumentException(String.format("Unknown DAO type %s", type));
        }
    }
}
