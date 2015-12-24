package eu.ioservices.plagio.spark.supplier;

import eu.ioservices.plagio.config.Configuration;
import eu.ioservices.plagio.model.Meta;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by u548850 on 11/23/2015.
 */
@FunctionalInterface
public interface ShinglesSupplier {
    static final String CFG_PARALLEL_FACTOR = "plagio.supply.hwcores";
    static final String CFG_INPUT_RAW_PATH = "plagio.supply.input.raw";
    static final String CFG_INPUT_CACHED_PATH = "plagio.supply.input.cached";

    JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Configuration cfg);
}
