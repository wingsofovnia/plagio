package eu.ioservices.plagio.spark.producer.file;

import eu.ioservices.plagio.config.Configuration;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.spark.producer.CacheProducer;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by u548850 on 11/23/2015.
 */
public class ObjectCacheProducer implements CacheProducer {

    @Override
    public void cache(JavaPairRDD<Integer, Meta> shingles, JavaSparkContext sparkContext, Configuration cfg) {

        String outputPath = cfg.getRequiredProperty(CFG_OUTPUT_CACHE_PATH) + "/" + System.currentTimeMillis() + "/";
        shingles.saveAsObjectFile(outputPath);
    }
}
