package ua.edu.sumdu.dl.spark.db;

import ua.edu.sumdu.dl.spark.PlachertConf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author superuser
 *         Created 06-Aug-15
 */
public class JdbcConnectionBuilder {
    private String driverName;
    private String connectionUrl;
    private String userName;
    private String password;

    public String getDriverName() {
        return driverName;
    }

    public JdbcConnectionBuilder setDriverName(String driverName) {
        this.driverName = driverName;
        return this;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public JdbcConnectionBuilder setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public JdbcConnectionBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JdbcConnectionBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public Connection build() throws SQLException, ClassNotFoundException {
        if (getConnectionUrl() == null || getDriverName() == null
                || getPassword() == null || getUserName() == null) {
            throw new IllegalStateException("Not all params has been set");
        }

        Class.forName(getDriverName());
        return DriverManager.getConnection(getConnectionUrl(), getUserName(), getPassword());
    }

    public static Connection build(PlachertConf conf) throws SQLException, ClassNotFoundException {
        JdbcConnectionBuilder connectionBuilder = new JdbcConnectionBuilder();
        connectionBuilder.setConnectionUrl(conf.getDatabaseConnectionUrl())
                         .setDriverName(conf.getDatabaseDriverName())
                         .setUserName(conf.getDatabaseUsername())
                         .setPassword(conf.getDatabasePassword());
        return connectionBuilder.build();

    }
}
