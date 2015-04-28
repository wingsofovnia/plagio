package ua.edu.sumdu.dl.spark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.edu.sumdu.dl.parsing.processor.StringProcessManager;
import ua.edu.sumdu.dl.spark.db.JDBCConnectionSupplier;

import java.io.Serializable;
import java.sql.Connection;

/**
 * @author superuser
 *         Created 26-Apr-15
 */
public class PlachertContext implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(PlachertContext.class);

    private final PlachertConf configuration;
    private JDBCConnectionSupplier connectionSupplier;
    private StringProcessManager stringProcessManager;

    public PlachertContext(PlachertConf plachertConf) {
        this.configuration = plachertConf;

        buildConnectionSupplier();
        buildStringProcessor();
    }

    private void buildConnectionSupplier() {
        try {
            connectionSupplier = new JDBCConnectionSupplier(configuration.getDatabaseDriverName(),
                                                            configuration.getDatabaseConnectionUrl(),
                                                            configuration.getDatabaseUsername(),
                                                            configuration.getDatabasePassword());
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to create JDBCConnectionSupplier", e);
        }
    }

    private void buildStringProcessor() {
        this.stringProcessManager = new StringProcessManager(configuration.getParsingProcessors());
    }

    public JDBCConnectionSupplier getConnectionSupplier() {
        return connectionSupplier;
    }

    public Connection getJDBCConnection() {
        return connectionSupplier.getConnection();
    }

    public PlachertConf getConfiguration() {
        return configuration;
    }

    public StringProcessManager getStringProcessManager() {
        return stringProcessManager;
    }


}
