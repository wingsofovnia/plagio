package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.PlagioException;
import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import eu.ioservices.plagio.config.Configuration;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.spark.supplier.ShinglesSupplier;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by u548850 on 11/23/2015.
 */
public class DocumentShinglesSupplier implements ShinglesSupplier {
    public static final String CFG_DB_TABLE_NAME = "plagio.supplier.db.input.raw.table";
    public static final String CFG_DB_PARTITION_COLUMN = "plagio.supplier.db.input.raw.primaryKey";

    @Override
    public JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Configuration cfg) {
        String tableName = cfg.getRequiredProperty(CFG_DB_TABLE_NAME);
        String primaryKey = cfg.getRequiredProperty(CFG_DB_PARTITION_COLUMN);
        try {
            return DataFrameUtils.buildDataFrame(sparkContext, tableName, primaryKey, cfg).javaRDD().flatMapToPair(row -> {
                String documentId = row.getString(0);
                String documentContent = row.getString(1);

                ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(documentContent);
                Set<Integer> shingles = shinglesAlgorithm.getDistinctHashedShingles();
                int shinglesAmount = shingles.size();

                return shingles.stream()
                        .map(shingle -> new Tuple2<>(shingle, new Meta(documentId, shinglesAmount, false)))
                        .collect(Collectors.toSet());
            });
        } catch (Exception e) {
            throw new PlagioException(e);
        }
    }
}
