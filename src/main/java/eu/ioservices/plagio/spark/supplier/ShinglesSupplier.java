package eu.ioservices.plagio.spark.supplier;

import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.model.Meta;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by u548850 on 11/23/2015.
 */
@FunctionalInterface
public interface ShinglesSupplier {
    JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Config cfg);
}
