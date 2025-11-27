package com.monolatte.kontur.model.SQL;

import java.sql.Connection;
import java.util.List;

public class SQLTableManager {
    private static class Singleton {
        private static final SQLTableManager INSTANCE = new SQLTableManager();
    }

    private final ComponentsDAO _componentsDAO;
    private final Components_usageDAO _componentsUsageManager;
    private final ProjectDAO _projectDAO;
    private final ManufacturerDAO _manufacturerDAO;
    private final Manufacturer_usageDAO _manufacturerUsageDAO;
    private final UserDAO _userDAO;
    private final User_usageDAO _userUsageDAO;
    private final List<ISQLDAOBase> _allManagers;

    private SQLTableManager() {
        Connection sharedConnection = SQLConnector.getConnection();

        this._componentsDAO = new ComponentsDAO("Components", sharedConnection);
        this._componentsUsageManager = new Components_usageDAO("Component_Usage", sharedConnection);
        this._projectDAO = new ProjectDAO("Projects", sharedConnection);
        this._manufacturerDAO = new ManufacturerDAO("Manufacturers", sharedConnection);
        this._manufacturerUsageDAO = new Manufacturer_usageDAO("Manufacturer_Usage", sharedConnection);
        this._userDAO = new UserDAO("Users", sharedConnection);
        this._userUsageDAO = new User_usageDAO("User_Usage", sharedConnection);

        _allManagers = List.of(
                this._componentsDAO,
                this._projectDAO,
                this._componentsUsageManager,
                this._manufacturerDAO,
                this._manufacturerUsageDAO,
                this._userDAO,
                this._userUsageDAO
        );
    }

    public ComponentsDAO getComponentsManager() {
        return _componentsDAO;
    }

    public Components_usageDAO getComponentsUsageManager() {
        return _componentsUsageManager;
    }

    public ProjectDAO getProjectManager() {
        return _projectDAO;
    }

    public ManufacturerDAO getManufacturerDAO() { return _manufacturerDAO; }

    public Manufacturer_usageDAO getManufacturerUsageDAO() { return _manufacturerUsageDAO; }

    public UserDAO getUserDAO() { return _userDAO; }

    public User_usageDAO getuserUsageDAO() { return _userUsageDAO; }

    public List<ISQLDAOBase> getAllManagers() { return this._allManagers; }

    public static SQLTableManager getInstance() {
        return Singleton.INSTANCE;
    }
}
