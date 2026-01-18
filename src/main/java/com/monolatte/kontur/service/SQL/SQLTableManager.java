package com.monolatte.kontur.service.SQL;

import com.monolatte.kontur.service.SQL.DAO.ComponentsDAO;
import com.monolatte.kontur.service.SQL.Interfaces.ISQLDAOBase;

import java.sql.Connection;
import java.util.List;

public class SQLTableManager {
    private static class Singleton {
        private static final SQLTableManager INSTANCE = new SQLTableManager();
    }

    private final ComponentsDAO _componentsDAO;
    private final List<ISQLDAOBase> _allManagers;

    private SQLTableManager() {
        Connection sharedConnection = SQLConnector.getConnection();

        this._componentsDAO = new ComponentsDAO("Components", sharedConnection);

        _allManagers = List.of(
                this._componentsDAO
        );
    }

    public ComponentsDAO getComponentsManager() {
        return _componentsDAO;
    }

    public List<ISQLDAOBase> getAllManagers() { return this._allManagers; }

    public static SQLTableManager getInstance() {
        return Singleton.INSTANCE;
    }
}
