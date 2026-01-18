package com.monolatte.kontur.service.SQL.DAO;

import com.monolatte.kontur.service.SQL.Interfaces.ISQLDAO;
import com.monolatte.kontur.service.SQL.SQLTableManager;

public class DAOFactory {

    public enum DAOType {
        COMPONENT
    }

    public static ISQLDAO<?> createDAO(DAOType type) {
        switch (type) {
            case COMPONENT -> {
                return SQLTableManager.getInstance().getComponentsManager();
            }
            default -> throw new IllegalArgumentException(String.format("Unknown DAO type %s", type));
        }
    }
}
