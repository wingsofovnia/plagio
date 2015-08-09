package ua.edu.sumdu.dl.spark;

import ua.edu.sumdu.dl.parsing.processor.StringProcessManager;
import ua.edu.sumdu.dl.spark.db.JdbcConnectionBuilder;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author superuser
 *         Created 26-Apr-15
 */
public class PlachertContext implements Serializable {

    private final PlachertConf configuration;
    private final StringProcessManager stringProcessManager;

    public PlachertContext(PlachertConf plachertConf) {
        this.configuration = plachertConf;
        this.stringProcessManager = new StringProcessManager(configuration.getParsingProcessors());
    }

    public PlachertConf getConfiguration() {
        return configuration;
    }

    public StringProcessManager getStringProcessManager() {
        return stringProcessManager;
    }

    public Connection buildJdbcConnection() throws SQLException, ClassNotFoundException {
        return JdbcConnectionBuilder.build(configuration);
    }
}
