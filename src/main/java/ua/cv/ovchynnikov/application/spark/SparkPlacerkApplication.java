package ua.cv.ovchynnikov.application.spark;

import org.apache.hadoop.mapred.InvalidInputException;
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
        final Boolean isCacheEnabled = appConf.getProperty(Key.APP_CACHE, "false").asBoolean();
        final Boolean isDebugEnabled = appConf.getProperty(Key.APP_DEBUG, "true").asBoolean();

        if (isDebugEnabled) {
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
        LOGGER.info("Creating Spark Context ...");
        SparkConf sparkConf = new SparkConf().setAppName(SPARK_APP_NAME);
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        LOGGER.info("Spark Context has been successfully created.");

        // [#S1] Supplying new shingles to Spark system
        LOGGER.info("Supplying new shingles to Spark system ... ");
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
        if (isCacheEnabled) {
            LOGGER.info("Cache system enabled. Supplying old shingles to Spark system.");
            final PlacerkConf.Property cacheDirProp = appConf.getProperty(Key.APP_IO_CACHE, null);
            if (cacheDirProp != null) {
                final String cacheDir = cacheDirProp.asString() + "*";
                LOGGER.debug("Cache dir = " + cacheDir);

                try {
                    JavaRDD<String> shglStrings = sc.textFile(cacheDir, appConf.getProperty(Key.APP_HWCORES).asInt());
                    JavaPairRDD<Integer, Meta> shingles = shglStrings.mapToPair(strHash -> new Tuple2<>(Integer.valueOf(strHash), (Meta) new ShingleMeta()));

                    if (isDebugEnabled) {
                        LOGGER.debug("Loaded cached records = " + shingles.count());
                    }
                    // [#2] Uniting old shingles and newly created from documents RDDs'
                    shingleMetaPairRDD = shingleMetaPairRDD.union(shingles);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load cache data: " + e.getMessage());
                }
            } else {
                LOGGER.error("Cache enabled, but cache dir is undefined");
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
        if (isCacheEnabled) {
            LOGGER.info("Saving new shingles into cache ... ");
            JavaRDD<Integer> newShingles = groupedUnitedShingleMeta.filter(shingleMetas -> ((Collection<?>) shingleMetas._2()).size() == 1)
                                                                   .map(shingleMetas -> shingleMetas._1());
            if (isDebugEnabled) {
                long newRecords = newShingles.count();
                LOGGER.debug("New shingles from new documents = " + newRecords);
            }

            String outputPath = appConf.getProperty(Key.APP_IO_CACHE).asString() + "/" + System.currentTimeMillis() + "/";
            newShingles.saveAsTextFile(outputPath);
            LOGGER.info("New shingles has been saved into cache dir.");
        } else {
            LOGGER.info("Cache mode is disabled, no documents' shingles will be cached.");
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