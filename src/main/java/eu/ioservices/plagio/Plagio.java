package eu.ioservices.plagio;

import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.converting.TextConverter;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.model.Result;
import eu.ioservices.plagio.processing.TextProcessorManager;
import eu.ioservices.plagio.spark.producer.CacheProducer;
import eu.ioservices.plagio.spark.supplier.ShinglesSupplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Apache Spark implementation that uses {@link eu.ioservices.plagio.algorithm.ShinglesAlgorithm}
 * for determining documents' duplication level in Apache Spark Cluster.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class Plagio {
    private static final Logger LOGGER = LogManager.getLogger(Plagio.class);
    private Config config;
    private boolean textProcessorManagerEnabled;
    private TextProcessorManager textProcessorManager;
    private boolean textConverterEnabled;
    private TextConverter textConverter;
    private ShinglesSupplier documentShinglesSupplier;
    private ShinglesSupplier cachedShinglesSupplier;
    private CacheProducer shinglesCacheProducer;

    protected Plagio(PlagioBuilder plagioBuilder) {
        this.config = Objects.requireNonNull(plagioBuilder.config);
        this.textProcessorManagerEnabled = plagioBuilder.textProcessorManagerEnabled;
        this.textProcessorManager = Objects.requireNonNull(plagioBuilder.textProcessorManager);
        this.textConverterEnabled = plagioBuilder.textConverterEnabled;
        this.textConverter = Objects.requireNonNull(plagioBuilder.textConverter);
        this.documentShinglesSupplier = Objects.requireNonNull(plagioBuilder.documentShinglesSupplier);
        this.cachedShinglesSupplier = Objects.requireNonNull(plagioBuilder.cachedShinglesSupplier);
        this.shinglesCacheProducer = Objects.requireNonNull(plagioBuilder.shinglesCacheProducer);
    }

    public List<Result> process() {
        if (config.isDebug()) {
            LOGGER.info("Debug mode is ON");
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
            ctx.updateLoggers(conf);
        }

        if (!config.isVerbose()) {
            org.apache.log4j.Logger.getLogger("org").setLevel(org.apache.log4j.Level.OFF);
            org.apache.log4j.Logger.getLogger("akka").setLevel(org.apache.log4j.Level.OFF);
        } else {
            LOGGER.info("Verbose mode ON. Spark logging enabled.");
        }

        // [Init] Building contexts
        LOGGER.info("Creating Spark Context ...");
        SparkConf sparkConf = new SparkConf().setAppName(config.getSparkAppName())
                .setMaster(config.getSparkAppMaster());
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        LOGGER.info("Spark Context has been successfully created.");

        // [#S1] Supplying new shingles to Spark system
        LOGGER.info("Supplying new shingles to Spark system ... ");
        JavaPairRDD<Integer, Meta> shingleMeta = documentShinglesSupplier.supply(sc, config);

        // [#S1] Supplying old shingles to Spark system
        if (config.isCaching()) {
            LOGGER.info("Cache system enabled. Supplying old shingles to Spark system.");
            JavaPairRDD<Integer, Meta> cachedShingles = cachedShinglesSupplier.supply(sc, config);
            shingleMeta = shingleMeta.union(cachedShingles);
        }

        // [#3] Grouping documents with same shingles
        JavaPairRDD<Integer, Iterable<Meta>> groupedUnitedShingleMeta = shingleMeta.groupByKey().cache();

        // [#4] Filtering tuples that don’t have at least one document we are checking
        JavaPairRDD<Integer, Iterable<Meta>> filteredGroupedUnitedShingleMeta = groupedUnitedShingleMeta
                .filter(shingleDocumentMetaTuple -> {
                    Iterable<Meta> unfilteredDocMetas = shingleDocumentMetaTuple._2();
                    return StreamSupport.stream(unfilteredDocMetas.spliterator(), false).anyMatch(e -> !e.isCached());
                });

        // [#5] Saving new shingles into
        if (config.isCaching()) {
            LOGGER.info("Saving new shingles into cache ... ");
            JavaPairRDD<Integer, Meta> newShingles = groupedUnitedShingleMeta
                    .filter(shingleMetas -> ((Collection<?>) shingleMetas._2()).size() == 1)
                    .mapToPair(shingleMetas -> new Tuple2<>(shingleMetas._1(), shingleMetas._2().iterator().next()));

            if (config.isDebug()) {
                long newRecords = newShingles.count();
                LOGGER.debug("New shingles from new documents = " + newRecords);
            }

            shinglesCacheProducer.cache(newShingles, sc, config);
            LOGGER.info("New shingles has been saved into cache dir.");
        } else {
            LOGGER.info("Cache mode is disabled, no documents' shingles will be cached.");
        }

        // [#6] Mapping documents with 1, if in their tuple are neighbors (duplicated shingles), 0 if no. Filtering non-marked documents
        JavaPairRDD<Meta, Integer> metaCoincides = filteredGroupedUnitedShingleMeta.flatMapToPair(shingleMetasTuple -> {
            Iterable<Meta> metas = shingleMetasTuple._2();
            int coincides = (int) StreamSupport.stream(metas.spliterator(), false)
                    .count();
            return StreamSupport.stream(metas.spliterator(), true)
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

    public static PlagioBuilder builder() {
        return new PlagioBuilder();
    }

    public static class PlagioBuilder {
        private Config config;
        private boolean textProcessorManagerEnabled;
        private TextProcessorManager textProcessorManager;
        private boolean textConverterEnabled;
        private TextConverter textConverter;
        private ShinglesSupplier documentShinglesSupplier;
        private ShinglesSupplier cachedShinglesSupplier;
        private CacheProducer shinglesCacheProducer;

        public PlagioBuilder config(Config config) {
            this.config = config;
            return this;
        }

        public PlagioBuilder textProcessorManager(TextProcessorManager textProcessorManager) {
            this.textProcessorManager = textProcessorManager;
            this.textProcessorManagerEnabled = true;
            return this;
        }

        public PlagioBuilder textConverter(TextConverter textConverter) {
            this.textConverter = textConverter;
            this.textConverterEnabled = true;
            return this;
        }

        public PlagioBuilder documentShinglesSupplier(ShinglesSupplier documentShinglesSupplier) {
            this.documentShinglesSupplier = documentShinglesSupplier;
            return this;
        }

        public PlagioBuilder cachedShinglesSupplier(ShinglesSupplier cachedShinglesSupplier) {
            this.cachedShinglesSupplier = cachedShinglesSupplier;
            return this;
        }

        public PlagioBuilder shinglesCacheProducer(CacheProducer shinglesCacheProducer) {
            this.shinglesCacheProducer = shinglesCacheProducer;
            return this;
        }

        public PlagioBuilder textProcessorManagerEnabled(boolean textProcessorManagerEnabled) {
            this.textProcessorManagerEnabled = textProcessorManagerEnabled;
            return this;
        }

        public PlagioBuilder textConverterEnabled(boolean textConverterEnabled) {
            this.textConverterEnabled = textConverterEnabled;
            return this;
        }

        public Plagio build() {
            return new Plagio(this);
        }
    }
}