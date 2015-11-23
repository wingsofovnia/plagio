package eu.ioservices.plagio.spark.producer.file;

import eu.ioservices.plagio.PlagioException;
import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.config.SparkFileSystemConfig;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.spark.producer.CacheProducer;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by u548850 on 11/23/2015.
 */
public class ObjectCacheProducer implements CacheProducer {

    @Override
    public void cache(JavaPairRDD<Integer, Meta> shingles, JavaSparkContext sparkContext, Config cfg) {
        if (!(cfg instanceof SparkFileSystemConfig))
            throw new PlagioException("TextShinglesSupplier requires SparkFileSystemConfig, but received " + cfg.getClass().getCanonicalName());

        SparkFileSystemConfig sparkFileSystemConfig = (SparkFileSystemConfig) cfg;
        String outputPath = sparkFileSystemConfig.getCachePath() + "/" + System.currentTimeMillis() + "/";
        shingles.saveAsObjectFile(outputPath);
    }
}
