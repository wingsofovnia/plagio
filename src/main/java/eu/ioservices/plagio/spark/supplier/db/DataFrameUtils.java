package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.PlagioException;
import eu.ioservices.plagio.config.Configuration;
import eu.ioservices.plagio.spark.supplier.ShinglesSupplier;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SQLContext;

import java.sql.*;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 24-Dec-15
 */
public abstract class DataFrameUtils {
    private static class Bounds {
        private static final int PARTITIONS_LOAD_FACTOR = 10;
        int upper;
        int lower;
        int partitions;
    }

    public static final String CFG_DB_DRIVER = "plagio.supplier.db.driver";
    public static final String CFG_DB_CONNECTION_URL = "plagio.supplier.db.connectionUrl";
    public static final String CFG_DB_USERNAME = "plagio.supplier.db.username";
    public static final String CFG_DB_PASSWORD = "plagio.supplier.db.password";
    public static final String CFG_DB_AUTO_BOUNDING = "plagio.supplier.db.bounding.auto";
    public static final String CFG_DB_UPPER_BOUND = "plagio.supplier.db.bounding.upperBound";
    public static final String CFG_DB_LOWER_BOUND = "plagio.supplier.db.bounding.lowerBound";
    public static final String CFG_DB_PARTITIONS = "plagio.supplier.db.bounding.partitions";

    public static DataFrame buildDataFrame(JavaSparkContext sparkContext, String table, String primaryKey, Configuration config) throws SQLException, ClassNotFoundException {
        SQLContext sqlContext = new SQLContext(sparkContext);

        String connUrl = config.getRequiredProperty(CFG_DB_CONNECTION_URL) +
                "?user=" + config.getRequiredProperty(CFG_DB_USERNAME) +
                "&password=" + config.getRequiredProperty(CFG_DB_PASSWORD);

        DataFrameReader reader = sqlContext.read()
                .format("jdbc")
                .option("driver", config.getRequiredProperty(CFG_DB_DRIVER))
                .option("url", connUrl)
                .option("dbtable", table);

        String upperBound, lowerBound, partitions;
        if (config.getRequiredProperty(CFG_DB_AUTO_BOUNDING, Boolean.class)) {
            Bounds bounds = calculateBounds(table, primaryKey, config);
            upperBound = String.valueOf(bounds.upper);
            lowerBound = String.valueOf(bounds.lower);
            partitions = String.valueOf(bounds.partitions);
        } else {
            upperBound = config.getRequiredProperty(CFG_DB_UPPER_BOUND);
            lowerBound = config.getRequiredProperty(CFG_DB_LOWER_BOUND);
            partitions = config.getRequiredProperty(CFG_DB_PARTITIONS);
        }

        reader.option("partitionColumn", primaryKey)
                .option("lowerBound", lowerBound)
                .option("upperBound", upperBound)
                .option("numPartitions", partitions);

        return reader.load();
    }

    private static Bounds calculateBounds(String tableName, String primaryKey, Configuration config) throws SQLException, ClassNotFoundException {
        Class.forName(config.getRequiredProperty(CFG_DB_DRIVER));

        Bounds bounds = new Bounds();
        try (final Connection connection = DriverManager.getConnection(config.getRequiredProperty(CFG_DB_CONNECTION_URL),
                config.getRequiredProperty(CFG_DB_USERNAME), config.getRequiredProperty(CFG_DB_PASSWORD))) {

            String lowerBoundQuery = "SELECT MIN(" + primaryKey + ") FROM " + tableName;
            try (Statement lowerBoundStatement = connection.createStatement()) {
                try (ResultSet lowerBoundResultSet = lowerBoundStatement.executeQuery(lowerBoundQuery)) {
                    if (!lowerBoundResultSet.next())
                        throw new PlagioException("Failed to estimate lower bound in tableName");

                    lowerBoundResultSet.first();
                    bounds.lower = lowerBoundResultSet.getInt(1);
                }
            }


            String upperBoundQuery = "SELECT MAX(" + primaryKey + ") FROM " + tableName;
            try (Statement upperBoundStatement = connection.createStatement()) {
                try (ResultSet upperBoundResultSet = upperBoundStatement.executeQuery(upperBoundQuery)) {
                    if (!upperBoundResultSet.next())
                        throw new PlagioException("Failed to estimate upper bound in tableName");

                    upperBoundResultSet.first();
                    bounds.upper = upperBoundResultSet.getInt(1);
                }
            }

            int approximateRecords = bounds.upper - bounds.lower;
            int hwCores = config.getRequiredProperty(ShinglesSupplier.CFG_PARALLEL_FACTOR, Integer.class);
            int differenceFactor = approximateRecords / hwCores;

            if (differenceFactor > Bounds.PARTITIONS_LOAD_FACTOR) {
                bounds.partitions = hwCores * (differenceFactor / Bounds.PARTITIONS_LOAD_FACTOR);
            } else {
                bounds.partitions = hwCores;
            }
        }


        return bounds;
    }
}
