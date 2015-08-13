package ua.cv.ovchynnikov.application.spark;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import ua.cv.ovchynnikov.application.PlacerkApplication;
import ua.cv.ovchynnikov.application.config.PlacerkConf;
import ua.cv.ovchynnikov.pojo.DocumentMeta;
import ua.cv.ovchynnikov.pojo.Meta;
import ua.cv.ovchynnikov.pojo.Result;
import ua.cv.ovchynnikov.pojo.ShingleMeta;
import ua.cv.ovchynnikov.processing.algorithm.ShinglesAlgorithm;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ua.cv.ovchynnikov.application.config.PlacerkConf.Key;

/**
 * @author superuser
 *         Created 27-Mar-15
 */
public class SparkPlacerkApplication implements PlacerkApplication {
    private static final String SPARK_APP_NAME = "io.SparkPlacerkApplication";
    private static final Logger LOGGER = LogManager.getLogger(SparkPlacerkApplication.class);

    @Override
    public List<Result> run(PlacerkConf appConf) {
        PlacerkConf.Property debug = appConf.getProperty(Key.APP_DEBUG);
        if (debug != null && debug.asBoolean()) {
            LOGGER.info("Debug mode is ON");
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
            ctx.updateLoggers(conf);
        }

        PlacerkConf.Property verbose = appConf.getProperty(Key.APP_VERBOSE);
        if (verbose == null || !verbose.asBoolean()) {
            org.apache.log4j.Logger.getLogger("org").setLevel(org.apache.log4j.Level.OFF);
            org.apache.log4j.Logger.getLogger("akka").setLevel(org.apache.log4j.Level.OFF);
        } else {
            LOGGER.info("Verbose mode ON. Spark logging enabled.");
        }

        // [Init] Building contexts
        LOGGER.info("Creating Spark Context, running Spark driver ...");
        SparkConf sparkConf = new SparkConf().setAppName(SPARK_APP_NAME);
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        LOGGER.info("Spark Context has been successfully created.");

        // [#S1] Supplying new shingles to Spark system
        JavaPairRDD<String, String> docPathContentPRDD = sc.wholeTextFiles(appConf.getProperty(Key.APP_IO_INPUT).asString(),
                appConf.getProperty(Key.APP_HWCORES).asInt());

        JavaPairRDD<Integer, Meta> shingleMetaPairRDD = docPathContentPRDD.flatMapToPair(pathContent -> {
            String path = pathContent._1();
            String content = pathContent._2();
            ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(content, appConf.getProperty(Key.APP_ALG_SHINGLE_SIZE).asInt());

            Set<Integer> hashedShingles = shinglesAlgorithm.getDistinctHashedShingles();

            return hashedShingles.stream()
                    .map(shingle -> new Tuple2<>(shingle, (Meta) new DocumentMeta(path, hashedShingles.size())))
                    .collect(Collectors.toCollection(LinkedList::new));
        });

        // [#S1] Supplying old shingles to Spark system
        if (appConf.hasProperty(Key.APP_IO_CACHE)) {
            String cacheDir = appConf.getProperty(Key.APP_IO_CACHE).asString();

            File[] hadoopFiles = new File(cacheDir).listFiles(File::isDirectory);
            if (hadoopFiles.length > 0) {
                String filesToLoad = String.join(",", Arrays.stream(hadoopFiles)
                        .map(f -> cacheDir + File.separator + f.getName() + File.separator)
                        .toArray(String[]::new));

                JavaRDD<String> shglStrings = sc.textFile(filesToLoad, appConf.getProperty(Key.APP_HWCORES).asInt());

                JavaPairRDD<Integer, Meta> shingles = shglStrings.mapToPair(strHash -> new Tuple2<>(Integer.valueOf(strHash), (Meta) new ShingleMeta()));

                LOGGER.error("OLD: " + shingles.count());
                // [#2] Uniting old shingles and newly created from documents RDDs'
                shingleMetaPairRDD = shingleMetaPairRDD.union(shingles);
                LOGGER.error(filesToLoad);
            } else {
                LOGGER.warn("No cached shingles found");
            }
        }

        // [#3] Grouping documents with same shingles
        JavaPairRDD<Integer, Iterable<Meta>> groupedUnitedShingleMeta = shingleMetaPairRDD.groupByKey().cache();

        // [#4] Filtering tuples that don’t have tag of the document we are checking (marked)
        JavaPairRDD<Integer, Iterable<Meta>> filteredGroupedUnitedShingleMeta = groupedUnitedShingleMeta.filter(shingleDocumentMetaTuple -> {
            Iterable<Meta> unfilteredDocMetas = shingleDocumentMetaTuple._2();
            return StreamSupport.stream(unfilteredDocMetas.spliterator(), false).anyMatch(m -> (m instanceof DocumentMeta));
        });

        // [#5] Saving new shingles into
        PlacerkConf.Property noSave = appConf.getProperty(Key.APP_CACHE);
        if (noSave == null || !noSave.asBoolean()) {
            LOGGER.info("Saving new shingles into cache");
            JavaRDD<Integer> newShingles = groupedUnitedShingleMeta.filter(shingleMetas -> ((Collection<?>) shingleMetas._2()).size() == 1)
                    .map(shingleMetas -> shingleMetas._1())
                    .cache();

            long newRecords = newShingles.count();
            LOGGER.info("New records: " + newRecords);
            if (newRecords > 0) {
                String outputPath = appConf.getProperty(Key.APP_IO_CACHE).asString() + "/" + System.currentTimeMillis() + "/";
                newShingles.saveAsTextFile(outputPath);
            } else {
                LOGGER.info("No new shingles has been cached");
            }
        } else {
            LOGGER.info("No-save mode is ENABLED, no documents' shingles will be cached.");
        }

        // [#6] Mapping documents with 1, if in their tuple are neighbors (duplicated shingles), 0 if no. Filtering non-marked documents
        JavaPairRDD<DocumentMeta, Integer> metaCoincides = filteredGroupedUnitedShingleMeta.flatMapToPair(shingleMetasTuple -> {
            Iterable<Meta> metas = shingleMetasTuple._2();
            int coincides = (int) StreamSupport.stream(metas.spliterator(), false)
                    .count();
            return StreamSupport.stream(metas.spliterator(), false)
                                .filter(e -> (e instanceof DocumentMeta))
                                .map(e -> new Tuple2<>((DocumentMeta) e, coincides > 1 ? 1 : 0))
                                .collect(Collectors.toList());
        });

        // [#7] Reducing, calculating coincidences.
        JavaPairRDD<DocumentMeta, Integer> reducedMetaCoincides = metaCoincides.reduceByKey((i1, i2) -> i1 + i2);

        // [#8] Creating Results, calculating duplication level
        JavaRDD<Result> resultsRDD = reducedMetaCoincides.map(metaCoincidesTuple ->
                new Result(metaCoincidesTuple._1(), metaCoincidesTuple._2()));

        return resultsRDD.collect();
    }
}