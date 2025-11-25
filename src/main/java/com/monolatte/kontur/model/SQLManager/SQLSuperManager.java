package com.monolatte.kontur.model.SQLManager;

import java.sql.Connection;
import java.util.List;

public class SQLSuperManager {
    private static class Singleton {
        private static final SQLSuperManager INSTANCE = new SQLSuperManager();
    }

    private final ComponentsManager _componentsManager;
    private final Components_usageManager _componentsUsageManager;
    private final ProjectManager _projectManager;
    private final List<ISQLManagerBase> _allManagers;

    private SQLSuperManager() {
        Connection sharedConnection = SQLConnector.getConnection();

        this._componentsManager = new ComponentsManager("Components", sharedConnection);
        this._componentsUsageManager = new Components_usageManager("Component_Usage", sharedConnection);
        this._projectManager = new ProjectManager("Projects", sharedConnection);

        _allManagers = List.of(
                this._componentsManager,
                this._projectManager,
                this._componentsUsageManager
        );
    }

    public ComponentsManager getComponentsManager() {
        return _componentsManager;
    }

    public Components_usageManager getComponentsUsageManager() {
        return _componentsUsageManager;
    }

    public ProjectManager getProjectManager() {
        return _projectManager;
    }

    public List<ISQLManagerBase> getAllManagers() { return this._allManagers; }

    public static SQLSuperManager getInstance() {
        return Singleton.INSTANCE;
    }
}
