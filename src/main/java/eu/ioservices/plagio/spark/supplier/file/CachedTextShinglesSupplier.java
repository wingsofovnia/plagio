package eu.ioservices.plagio.spark.supplier.file;

import eu.ioservices.plagio.PlagioException;
import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.config.SparkFileSystemConfig;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.spark.supplier.ShinglesSupplier;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * Created by u548850 on 11/23/2015.
 */
public class CachedTextShinglesSupplier implements ShinglesSupplier {

    @Override
    public JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Config cfg) {
        if (!(cfg instanceof SparkFileSystemConfig))
            throw new PlagioException("TextShinglesSupplier requires SparkFileSystemConfig, but received " + cfg.getClass().getCanonicalName());

        SparkFileSystemConfig sparkFileSystemConfig = (SparkFileSystemConfig) cfg;
        final JavaRDD<Object> objectFiles = sparkContext.objectFile(sparkFileSystemConfig.getCachePath(),
                sparkFileSystemConfig.getHwCores());

        return objectFiles.mapToPair(rawCachedObject -> {
            final Tuple2<Integer, Meta> cachedMetaTuple = (Tuple2<Integer, Meta>) rawCachedObject;
            cachedMetaTuple._2().setCached(true);
            return cachedMetaTuple;
        });
    }
}
