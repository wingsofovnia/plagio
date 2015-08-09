package ua.edu.sumdu.dl.spark.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import ua.edu.sumdu.dl.spark.PlachertConf;
import ua.edu.sumdu.dl.spark.PlachertContext;
import ua.edu.sumdu.dl.spark.PlachertException;
import ua.edu.sumdu.dl.spark.PlachertInitializationException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author superuser
 *         Created 05-Aug-15
 */
public class JdbcRddSupplier {
    private static final Logger LOGGER = LogManager.getLogger(JdbcRddSupplier.class);
    private static final int PARTITIONS_LOAD_FACTOR = 10;

    protected final JavaSparkContext sparkContext;
    private final PlachertContext plachertContext;

    private String tableName;
    private String primaryKey;

    private Integer lowerBound;
    private Integer upperBound;
    private boolean partitioning = false;


    public JdbcRddSupplier(JavaSparkContext sparkContext, PlachertContext plachertContext) {
        if (sparkContext == null || plachertContext == null)
            throw new IllegalArgumentException("Some args are null");
        this.sparkContext = sparkContext;
        this.plachertContext = plachertContext;
    }

    public JdbcRddSupplier from(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public JdbcRddSupplier primaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public JdbcRddSupplier setLowerBound(int lowerBound) {
        if (getUpperBound() != null && lowerBound > getUpperBound())
            throw new IllegalArgumentException("upperBound < lowerBound");
        this.lowerBound = lowerBound;
        setPartitioning(true);
        return this;
    }

    public JdbcRddSupplier setUpperBound(int upperBound) {
        if (getLowerBound() != null && upperBound < getLowerBound())
            throw new IllegalArgumentException("upperBound < lowerBound");
        this.upperBound = upperBound;
        setPartitioning(true);
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public Integer getLowerBound() {
        return lowerBound;
    }

    public Integer getUpperBound() {
        return upperBound;
    }

    public boolean isPartitioning() {
        return partitioning;
    }

    public void setPartitioning(boolean partitioning) {
        this.partitioning = partitioning;
    }

    public void calculateBounds() {
        if (getTableName() == null || getPrimaryKey() == null)
            throw new IllegalStateException("Not enough params have been set");

        try (Connection connection = plachertContext.buildJdbcConnection()) {

            String lowerBoundQuery = "SELECT MIN(" + getPrimaryKey() + ") FROM " + getTableName();
            Statement lowerBoundStatement = connection.createStatement();
            ResultSet lowerBoundResultSet = lowerBoundStatement.executeQuery(lowerBoundQuery);
            if (!lowerBoundResultSet.next())
                throw new PlachertInitializationException("Failed to estimate lower bound in tableName");
            lowerBoundResultSet.first();
            setLowerBound(lowerBoundResultSet.getInt(1));
            lowerBoundResultSet.close();
            lowerBoundStatement.close();

            String upperBoundQuery = "SELECT MAX(" + getPrimaryKey() + ") FROM " + getTableName();
            Statement upperBoundStatement = connection.createStatement();
            ResultSet upperBoundResultSet = upperBoundStatement.executeQuery(upperBoundQuery);
            if (!upperBoundResultSet.next())
                throw new PlachertInitializationException("Failed to estimate upper bound in tableName");
            upperBoundResultSet.first();
            setUpperBound(upperBoundResultSet.getInt(1));
            upperBoundResultSet.close();
            upperBoundStatement.close();
        } catch (SQLException sq) {
            throw new PlachertException("Failed to calculate upper bound", sq);
        } catch (ClassNotFoundException ce) {
            throw new PlachertException(ce);
        }
    }

    private int calculatePartitionsAmount() {
        if (getUpperBound() == null || getLowerBound() == null)
            throw new IllegalStateException("Some necessary ars are null");

        int clusterCores = plachertContext.getConfiguration().getClusterCores();
        int approximateRecords = getUpperBound() - getLowerBound();
        int differenceFactor = approximateRecords / clusterCores;

        int partitions;
        if (differenceFactor > PARTITIONS_LOAD_FACTOR) {
            partitions = clusterCores * (differenceFactor / PARTITIONS_LOAD_FACTOR);
        } else {
            partitions = clusterCores;
        }

        setPartitioning(true);
        return partitions;
    }

    public JavaRDD<Row> supply() {
        if (isPartitioning() && getUpperBound().equals(getLowerBound())) {
            LOGGER.error("!!!EMPTY RDDD :: isPartitioning() = {}, eq = {}, up={},low={}", isPartitioning(), getUpperBound().equals(getLowerBound()), getUpperBound(), getLowerBound());
            return sparkContext.emptyRDD();
        }
        LOGGER.debug("Retrieving data from table = {}, lower bound = {}, upper bound = {}, partitioning = {}", getTableName(), getLowerBound(), getUpperBound(), isPartitioning());

        Map<String, String> options = new HashMap<>();
        PlachertConf conf = plachertContext.getConfiguration();
        String connUrl = conf.getDatabaseConnectionUrl() + "?user=" + conf.getDatabaseUsername() + "&password=" + conf.getDatabasePassword();
        options.put("driver", conf.getDatabaseDriverName());
        options.put("url", connUrl);
        options.put("dbtable", getTableName());
        if (isPartitioning()) {
            if (getLowerBound() == null || getUpperBound() == null)
                throw new IllegalStateException("Some bounds are not set");

            options.put("partitionColumn", primaryKey);
            options.put("lowerBound", String.valueOf(getLowerBound()));
            options.put("upperBound", String.valueOf(getUpperBound()));
            options.put("numPartitions", String.valueOf(calculatePartitionsAmount()));
        }

        return new SQLContext(sparkContext).load("jdbc", options).javaRDD();
    }
}
