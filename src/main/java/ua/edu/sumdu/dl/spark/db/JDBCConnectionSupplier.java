package ua.edu.sumdu.dl.spark.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.runtime.AbstractFunction0;
import ua.edu.sumdu.dl.spark.PlachertInitializationException;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author superuser
 *         Created 23-Apr-15
 */
public class JDBCConnectionSupplier extends AbstractFunction0<Connection> implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(JDBCConnectionSupplier.class);
    private final String driverName;
    private final String connectionUrl;
    private final String userName;
    private final String password;

    public JDBCConnectionSupplier(String driverName, String connectionUrl, String userName, String password) throws ClassNotFoundException {
        this.driverName = driverName;
        this.connectionUrl = connectionUrl;
        this.userName = userName;
        this.password = password;
        LOGGER.debug("Object has been built: " + this.toString());
        initialize();
    }

    private void initialize() throws ClassNotFoundException {
        Class.forName(driverName);
        LOGGER.debug("Driver <{}> was loaded", driverName);
    }

    @Override
    public Connection apply() {
        try {
            LOGGER.debug("Returning connection: " + connectionUrl);
            return DriverManager.getConnection(connectionUrl, userName, password);
        } catch (SQLException e) {
            throw new PlachertInitializationException("Failed to create connection", e);
        }
    }

    public Connection getConnection() {
        return apply();
    }

    @Override
    public String toString() {
        return "JDBCConnectionSupplier{" +
                "driverName='" + driverName + '\'' +
                ", connectionUrl='" + connectionUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
