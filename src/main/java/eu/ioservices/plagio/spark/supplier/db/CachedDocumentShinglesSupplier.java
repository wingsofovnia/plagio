package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.config.SparkDatabaseConfig;
import eu.ioservices.plagio.model.Meta;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * Created by u548850 on 11/23/2015.
 */
public class CachedDocumentShinglesSupplier extends AbstractDatabaseShinglesSuppler {
    protected CachedDocumentShinglesSupplier(JavaSparkContext sparkContext, SparkDatabaseConfig config) {
        super(sparkContext, config);
    }

    @Override
    public JavaPairRDD<Integer, Meta> supply() {
        return buildDataFrame(config.getCacheTableName()).javaRDD().mapToPair(row -> {
            String shingleId = row.getString(0);
            String documentId = row.getString(1);
            Integer shingleContent = row.getInt(2);
            int shinglesAmount = row.getInt(3);

            return new Tuple2<>(shingleContent, new Meta(documentId, shinglesAmount, true));
        });
    }
}
