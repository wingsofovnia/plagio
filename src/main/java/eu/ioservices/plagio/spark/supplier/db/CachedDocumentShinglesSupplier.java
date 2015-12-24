package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.PlagioException;
import eu.ioservices.plagio.config.Configuration;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.spark.supplier.ShinglesSupplier;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * Created by u548850 on 11/23/2015.
 */
public class CachedDocumentShinglesSupplier implements ShinglesSupplier {
    public static final String CFG_DB_CACHE_TABLE_NAME = "plagio.supplier.db.input.cache.table";
    public static final String CFG_DB_CACHE_PARTITION_COLUMN = "plagio.supplier.db.input.cache.primaryKey";

    @Override
    public JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Configuration cfg) {
        String tableName = cfg.getRequiredProperty(CFG_DB_CACHE_TABLE_NAME);
        String primaryKey = cfg.getRequiredProperty(CFG_DB_CACHE_PARTITION_COLUMN);

        try {
            return DataFrameUtils.buildDataFrame(sparkContext, tableName, primaryKey, cfg).javaRDD().mapToPair(row -> {
                String shingleId = row.getString(0);
                String documentId = row.getString(1);
                Integer shingleContent = row.getInt(2);
                int shinglesAmount = row.getInt(3);

                return new Tuple2<>(shingleContent, new Meta(documentId, shinglesAmount, true));
            });
        } catch (Exception e) {
            throw new PlagioException(e);
        }
    }
}
