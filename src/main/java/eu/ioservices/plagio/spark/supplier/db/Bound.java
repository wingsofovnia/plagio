package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.config.SparkDatabaseConfig;
import eu.ioservices.plagio.PlagioException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Created by u548850 on 11/23/2015.
 */

public class Bound {
    private static final int PARTITIONS_LOAD_FACTOR = 10;
    int upper;
    int lower;
    int partitions;

    public Bound(String tableName, String tablePkColumn, SparkDatabaseConfig config) {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(tablePkColumn);

        try (Connection connection = ConnectionBuilder.build(config)) {
            String lowerBoundQuery = "SELECT MIN(" + tableName + ") FROM " + tablePkColumn;
            try (Statement lowerBoundStatement = connection.createStatement()) {
                try (ResultSet lowerBoundResultSet = lowerBoundStatement.executeQuery(lowerBoundQuery)) {
                    if (!lowerBoundResultSet.next())
                        throw new PlagioException("Failed to estimate lower bound in tableName");

                    lowerBoundResultSet.first();
                    this.lower = lowerBoundResultSet.getInt(1);
                }
            }


            String upperBoundQuery = "SELECT MAX(" + tableName + ") FROM " + tablePkColumn;
            try (Statement upperBoundStatement = connection.createStatement()) {
                try (ResultSet upperBoundResultSet = upperBoundStatement.executeQuery(upperBoundQuery)) {
                    if (!upperBoundResultSet.next())
                        throw new PlagioException("Failed to estimate upper bound in tableName");

                    upperBoundResultSet.first();
                    this.upper = upperBoundResultSet.getInt(1);
                }
            }

            int approximateRecords = this.upper - this.lower;
            int differenceFactor = approximateRecords / config.getHwCores();

            if (differenceFactor > PARTITIONS_LOAD_FACTOR) {
                this.partitions = config.getHwCores() * (differenceFactor / PARTITIONS_LOAD_FACTOR);
            } else {
                this.partitions = config.getHwCores();
            }
        } catch (SQLException sq) {
            throw new PlagioException("Failed to calculate upper bound", sq);
        } catch (ClassNotFoundException ce) {
            throw new PlagioException(ce);
        }
    }

    public int getUpper() {
        return upper;
    }

    public int getLower() {
        return lower;
    }

    public int getPartitions() {
        return partitions;
    }
}
