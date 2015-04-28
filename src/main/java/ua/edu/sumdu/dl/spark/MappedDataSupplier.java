package ua.edu.sumdu.dl.spark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.JdbcRDD;
import scala.reflect.ClassManifestFactory;
import ua.edu.sumdu.dl.spark.function.SparkFunction;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author superuser
 *         Created 23-Apr-15
 */
public class MappedDataSupplier<T> implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(MappedDataSupplier.class);
    private static final int PARTITIONS_LOAD_FACTOR = 10;

    protected final JavaSparkContext sparkContext;
    private final PlachertContext plachertContext;

    private final Class<T> clazz;

    private String tableName;
    private String primaryKey;

    private int lowerBound = 1;
    private int upperBound;
    private boolean calculateUpperBound = true;

    private final List<String> where = new ArrayList<>();

    public MappedDataSupplier(JavaSparkContext sparkContext, PlachertContext plachertContext, Class<T> clazz) {
        this.sparkContext = sparkContext;
        this.plachertContext = plachertContext;
        this.clazz = clazz;
    }

    public MappedDataSupplier<T> from(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public MappedDataSupplier<T> primaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public MappedDataSupplier<T> setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
        return this;
    }

    public MappedDataSupplier<T> setUpperBound(int upperBound) {
        if (upperBound < this.lowerBound)
            throw new IllegalArgumentException("upperBound < lowerBound");
        this.upperBound = upperBound;
        return this;
    }

    public MappedDataSupplier<T> autoUpperBound(boolean isAutoBound) {
        this.calculateUpperBound = isAutoBound;
        return this;
    }

    public MappedDataSupplier<T> where(String exp) {
        where.add(exp);
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public boolean isCalculateUpperBound() {
        return calculateUpperBound;
    }

    public List<String> getWhere() {
        return where;
    }

    private int calculateUppedBound() {
        try (Connection connection = plachertContext.getJDBCConnection()) {
            Statement statement = connection.createStatement();
            String query = "SELECT MAX(" + primaryKey + ") FROM " + tableName;
            ResultSet resultSet = statement.executeQuery(query);
            if (!resultSet.next())
                throw new PlachertInitializationException("Failed to count rows in tableName");
            resultSet.first();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new PlachertException("Failed to calculate upper bound", e);
        }
    }

    public JavaRDD<T> get(SparkFunction<ResultSet, T> sparkFunction) {
        if (this.isCalculateUpperBound()) {
            int rows = this.calculateUppedBound();
            if (rows <= getLowerBound()) {
                LOGGER.warn("Auto upper-bound calculating: RowCount == 0 for table {}, returning empty RDD", tableName);
                return sparkContext.emptyRDD();
            }
            this.setUpperBound(rows);
            LOGGER.debug("Auto upper-bound calculating: RowCount = {} for table {}", rows, getTableName());
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM ")
                    .append(tableName)
                    .append(" WHERE ");

        List<String> whereExpressions = getWhere();
        if (whereExpressions.size() > 0) {
            for (String where : whereExpressions) {
                queryBuilder.append(where)
                            .append(" AND ");
            }
        }
        queryBuilder.append(primaryKey)
                    .append(" >= ? AND ")
                    .append(primaryKey)
                    .append(" <= ?");

        String query = queryBuilder.toString();

        int clusterCores = plachertContext.getConfiguration().getClusterCores();
        int approximateRecords = getUpperBound() - getLowerBound();
        int differenceFactor = approximateRecords / clusterCores;

        int partitions;
        if (differenceFactor > PARTITIONS_LOAD_FACTOR)
            partitions = clusterCores * (differenceFactor / PARTITIONS_LOAD_FACTOR);
        else
            partitions = clusterCores;

        LOGGER.debug("Retrieving data with query = {}, lower bound = {}, upper bound = {}, partitions = {}", query, getLowerBound(), getUpperBound(), partitions);

        return new JdbcRDD<>(sparkContext.sc(), plachertContext.getConnectionSupplier(),
                query,
                getLowerBound(), getUpperBound(),
                partitions,
                sparkFunction,
                ClassManifestFactory.fromClass(clazz)).toJavaRDD();
    }
}
