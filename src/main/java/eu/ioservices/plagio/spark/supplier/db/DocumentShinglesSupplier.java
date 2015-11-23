package eu.ioservices.plagio.spark.supplier.db;

import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import eu.ioservices.plagio.config.SparkDatabaseConfig;
import eu.ioservices.plagio.model.Meta;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by u548850 on 11/23/2015.
 */
public class DocumentShinglesSupplier extends AbstractDatabaseShinglesSuppler {
    protected DocumentShinglesSupplier(JavaSparkContext sparkContext, SparkDatabaseConfig config) {
        super(sparkContext, config);
    }

    @Override
    public JavaPairRDD<Integer, Meta> supply() {
        return buildDataFrame(config.getDocTableName()).javaRDD().flatMapToPair(row -> {
            String documentId = row.getString(0);
            String documentContent = row.getString(1);

            ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(documentContent);
            Set<Integer> shingles = shinglesAlgorithm.getDistinctHashedShingles();
            int shinglesAmount = shingles.size();

            return shingles.stream()
                           .map(shingle -> new Tuple2<>(shingle, new Meta(documentId, shinglesAmount, false)))
                           .collect(Collectors.toSet());
        });
    }
}
