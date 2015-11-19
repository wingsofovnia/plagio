package eu.ioservices.plagio.core.processor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import eu.ioservices.plagio.config.AppConfiguration;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.model.Result;
import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static eu.ioservices.plagio.config.AppConfiguration.Key;

/**
 * Apache Spark {@link CoreProcessor} implementation that uses {@link eu.ioservices.plagio.algorithm.ShinglesAlgorithm}
 * for determining documents' duplication level in Apache Spark Cluster.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class SparkCoreProcessor implements CoreProcessor {
    private static final String SPARK_APP_NAME = "io.SparkPlacerkApplication";
    private static final Logger LOGGER = LogManager.getLogger(SparkCoreProcessor.class);

    @Override
    public List<Result> process(AppConfiguration appConfiguration) {
        final Boolean isCacheEnabled = appConfiguration.getProperty(Key.APP_CACHE, "false").asBoolean();
        final Boolean isDebugEnabled = appConfiguration.getProperty(Key.APP_DEBUG, "true").asBoolean();
        final Boolean isVerboseEnabled = appConfiguration.getProperty(Key.APP_VERBOSE, "false").asBoolean();

        if (isDebugEnabled) {
            LOGGER.info("Debug mode is ON");
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
            ctx.updateLoggers(conf);
        }

        if (!isVerboseEnabled) {
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
        JavaPairRDD<String, String> docPathContentPRDD = sc.wholeTextFiles(appConfiguration.getProperty(Key.APP_IO_INPUT).asString(),
                                                                           appConfiguration.getProperty(Key.APP_HWCORES).asInt());

        JavaPairRDD<Integer, Meta> shingleMeta = docPathContentPRDD.flatMapToPair(pathContent -> {
            String fileName = pathContent._1().substring(pathContent._1().lastIndexOf(File.separator) + 1);
            String content = pathContent._2();
            ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(content, appConfiguration.getProperty(Key.APP_ALG_SHINGLE_SIZE).asInt());

            List<Integer> hashedShingles = shinglesAlgorithm.getHashedShingles();

            return hashedShingles.stream()
                                 .map(shingle -> new Tuple2<>(shingle, new Meta(fileName, hashedShingles.size())))
                                 .collect(Collectors.toCollection(LinkedList::new));
        });

        // [#S1] Supplying old shingles to Spark system
        if (isCacheEnabled) {
            LOGGER.info("Cache system enabled. Supplying old shingles to Spark system.");
            final AppConfiguration.Property cacheDirProp = appConfiguration.getProperty(Key.APP_IO_CACHE, null);
            if (cacheDirProp != null) {
                final String cacheDir = cacheDirProp.asString() + "*";
                LOGGER.debug("Cache dir = " + cacheDir);

                try {
                    final JavaRDD<Object> rawCachedObjects = sc.objectFile(cacheDir, appConfiguration.getProperty(Key.APP_HWCORES).asInt());
                    final JavaPairRDD<Integer, Meta> cache = rawCachedObjects.flatMapToPair(rawCachedObject -> {
                        final Tuple2<Integer, Iterable<Meta>> cachedMetaTuple = (Tuple2<Integer, Iterable<Meta>>) rawCachedObject;
                        final Integer cachedShingle = cachedMetaTuple._1();
                        final Iterable<Meta> cachedMetas = cachedMetaTuple._2();

                        final List<Tuple2<Integer, Meta>> shingleMetaTuples = new LinkedList<>();

                        for (Meta cachedMeta : cachedMetas) {
                            cachedMeta.setCached(true);
                            shingleMetaTuples.add(new Tuple2<>(cachedShingle, cachedMeta));
                        }

                        return shingleMetaTuples;
                    });

                    if (isDebugEnabled) {
                        LOGGER.debug("Loaded cached records = " + cache.count());
                    }
                    // [#2] Uniting old shingles and newly created from documents RDDs'
                    shingleMeta = shingleMeta.union(cache);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load cache data: " + e.getMessage());
                    throw new CoreProcessingException(e);
                }
            } else {
                LOGGER.error("Cache enabled, but cache dir is undefined");
            }
        }

        // [#3] Grouping documents with same shingles
        JavaPairRDD<Integer, Iterable<Meta>> groupedUnitedShingleMeta = shingleMeta.groupByKey().cache();

        // [#4] Filtering tuples that don’t have at least one document we are checking
        JavaPairRDD<Integer, Iterable<Meta>> filteredGroupedUnitedShingleMeta = groupedUnitedShingleMeta.filter(shingleDocumentMetaTuple -> {
            Iterable<Meta> unfilteredDocMetas = shingleDocumentMetaTuple._2();
            return StreamSupport.stream(unfilteredDocMetas.spliterator(), false).anyMatch(e -> !e.isCached());
        });

        // [#5] Saving new shingles into
        if (isCacheEnabled) {
            LOGGER.info("Saving new shingles into cache ... ");
            final JavaPairRDD<Integer, Iterable<Meta>> toBeCache = groupedUnitedShingleMeta.filter(shingleMetas -> ((Collection<?>) shingleMetas._2()).size() == 1);

            if (isDebugEnabled) {
                long newRecords = toBeCache.count();
                LOGGER.debug("New shingles from new documents = " + newRecords);
            }

            String outputPath = appConfiguration.getProperty(Key.APP_IO_CACHE).asString() + "/" + System.currentTimeMillis() + "/";
            toBeCache.saveAsObjectFile(outputPath);
            LOGGER.info("New shingles has been saved into cache dir.");
        } else {
            LOGGER.info("Cache mode is disabled, no documents' shingles will be cached.");
        }

        // [#6] Mapping documents with 1, if in their tuple are neighbors (duplicated shingles), 0 if no. Filtering non-marked documents
        JavaPairRDD<Meta, Integer> metaCoincides = filteredGroupedUnitedShingleMeta.flatMapToPair(shingleMetasTuple -> {
            Iterable<Meta> metas = shingleMetasTuple._2();
            int coincides = (int) StreamSupport.stream(metas.spliterator(), false)
                                               .count();
            return StreamSupport.stream(metas.spliterator(), false)
                                .filter(e -> !e.isCached())
                                .map(e -> new Tuple2<>(e, coincides > 1 ? 1 : 0))
                                .collect(Collectors.toList());
        });

        // [#7] Reducing, calculating coincidences.
        JavaPairRDD<Meta, Integer> reducedMetaCoincides = metaCoincides.reduceByKey((i1, i2) -> i1 + i2);

        // [#8] Creating Results, calculating duplication level
        JavaRDD<Result> resultsRDD = reducedMetaCoincides.map(metaCoincidesTuple ->
                new Result(metaCoincidesTuple._1(), metaCoincidesTuple._2()));

        return resultsRDD.collect();
    }
}