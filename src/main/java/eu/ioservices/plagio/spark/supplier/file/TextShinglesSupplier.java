package eu.ioservices.plagio.spark.supplier.file;

import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import eu.ioservices.plagio.config.Configuration;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.spark.supplier.ShinglesSupplier;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by u548850 on 11/23/2015.
 */
public class TextShinglesSupplier implements ShinglesSupplier {

    @Override
    public JavaPairRDD<Integer, Meta> supply(JavaSparkContext sparkContext, Configuration cfg) {

        final JavaPairRDD<String, String> textFiles =
                sparkContext.wholeTextFiles(cfg.getRequiredProperty(CFG_INPUT_RAW_PATH),
                                            cfg.getRequiredProperty(CFG_PARALLEL_FACTOR, Integer.class));

        return textFiles.flatMapToPair(pathContent -> {
            String fileName = pathContent._1().substring(pathContent._1().lastIndexOf(File.separator) + 1);
            String content = pathContent._2();
            ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(content);

            List<Integer> hashedShingles = shinglesAlgorithm.getHashedShingles();

            return hashedShingles.stream()
                    .map(shingle -> new Tuple2<>(shingle, new Meta(fileName, hashedShingles.size())))
                    .collect(Collectors.toCollection(LinkedList::new));
        });
    }
}
