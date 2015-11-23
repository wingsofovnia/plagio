package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.config.SparkDatabaseConfig;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by u548850 on 11/23/2015.
 */
public class ConnectionBuilder implements Serializable {
    private String driverName;
    private String connectionUrl;
    private String userName;
    private String password;

    public ConnectionBuilder setDriverName(String driverName) {
        this.driverName = driverName;
        return this;
    }

    public ConnectionBuilder setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public ConnectionBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public ConnectionBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public Connection build() throws SQLException, ClassNotFoundException {
        if (connectionUrl == null || driverName == null
                || password == null || userName == null) {
            throw new IllegalStateException("Not all params has been set");
        }

        Class.forName(driverName);
        return DriverManager.getConnection(connectionUrl, userName, password);
    }

    public static Connection build(SparkDatabaseConfig databaseConfig) throws SQLException, ClassNotFoundException {
        return new ConnectionBuilder().setConnectionUrl(databaseConfig.getConnectionUrl())
                                      .setDriverName(databaseConfig.getDriverName())
                                      .setUserName(databaseConfig.getUserName())
                                      .setPassword(databaseConfig.getPassword())
                                      .build();
    }
}
