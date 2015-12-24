package eu.ioservices.plagio.spark.supplier.file;

import eu.ioservices.plagio.config.Configuration;
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
    public JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Configuration cfg) {
        final JavaRDD<Object> objectFiles =
                sparkContext.objectFile(cfg.getRequiredProperty(CFG_INPUT_CACHED_PATH),
                                        cfg.getRequiredProperty(CFG_PARALLEL_FACTOR, Integer.class));

        return objectFiles.mapToPair(rawCachedObject -> {
            final Tuple2<Integer, Meta> cachedMetaTuple = (Tuple2<Integer, Meta>) rawCachedObject;
            cachedMetaTuple._2().setCached(true);
            return cachedMetaTuple;
        });
    }
}
