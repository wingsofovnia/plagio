package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.config.SparkDatabaseConfig;
import eu.ioservices.plagio.spark.supplier.AbstractShinglesSupplier;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SQLContext;

/**
 * Created by u548850 on 11/23/2015.
 */
public abstract class AbstractDatabaseShinglesSuppler extends AbstractShinglesSupplier {

    protected SparkDatabaseConfig config;

    protected AbstractDatabaseShinglesSuppler(JavaSparkContext sparkContext, SparkDatabaseConfig config) {
        super(sparkContext);
        this.config = config;
    }

    protected DataFrame buildDataFrame(String table) {
        SQLContext sqlContext = new SQLContext(sparkContext);
        String connUrl = config.getConnectionUrl() + "?user=" + config.getUserName() + "&password=" + config.getPassword();

        DataFrameReader reader = sqlContext.read()
                                           .format("jdbc")
                                           .option("driver", config.getDriverName())
                                           .option("url", connUrl)
                                           .option("dbtable", table);
        if (config.getNumPartitions() > 0) {
            if (config.getLowerBound() < 0  || config.getUpperBound() <= config.getLowerBound())
                throw new IllegalStateException("Some bounds are not set");

            reader.option("partitionColumn", config.getDocTablePKColumn())
                  .option("lowerBound", String.valueOf(config.getLowerBound()))
                  .option("upperBound", String.valueOf(config.getUpperBound()))
                  .option("numPartitions", String.valueOf(config.getNumPartitions()));
        }

        return reader.load();
    }

    public SparkDatabaseConfig getConfig() {
        return config;
    }
}
