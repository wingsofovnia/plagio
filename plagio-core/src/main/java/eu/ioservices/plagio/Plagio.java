package eu.ioservices.plagio;

import eu.ioservices.plagio.algorithm.HashShinglesAlgorithm;
import eu.ioservices.plagio.model.DuplicationReport;
import eu.ioservices.plagio.model.Metadata;
import eu.ioservices.plagio.util.Texts;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.Closeable;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 27-Dec-15
 */
public class Plagio implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger(Plagio.class);
    public static final boolean DEFAULT_CHECK_DOCUMENT_UPDATES_LIB = false;
    public static final boolean DEFAULT_CHECK_DOCUMENT_NORMALIZES_TEXT = true;

    private final PlagioConfig config;
    private JavaSparkContext sparkContext;
    private boolean isSparkContextStopped = false;

    public Plagio(PlagioConfig config) {
        this.config = config;
    }

    public void updateLibrary(String inputPath) {
        final JavaPairRDD<Integer, Metadata> oldShingles = this.retrieveLibrary();
        final JavaPairRDD<Integer, Metadata> newShingles = this.supplyDocumentShingles(inputPath, true);

        final JavaPairRDD<Integer, Metadata> procSpace = oldShingles.union(newShingles);

        this.updateLibrary(procSpace);
    }

    public void updateLibrary(JavaPairRDD<Integer, Metadata> procSpace) {
        final JavaPairRDD<Integer, Metadata> newLibShingles = procSpace.distinct()
                .filter(shingleMetaTuple -> shingleMetaTuple._2().isMarked())
                .mapValues(metadata -> new Metadata(metadata.getDocumentId(), metadata.getTotalShingles()));

        if (!newLibShingles.isEmpty()) {
            String libOutputPath = config.getLibraryPath() + "/" + System.currentTimeMillis() + "/";
            newLibShingles.saveAsObjectFile(libOutputPath);
        }
    }

    public List<DuplicationReport> checkDocuments(String inputPath) {
        return this.checkDocuments(inputPath, DEFAULT_CHECK_DOCUMENT_UPDATES_LIB);
    }

    public List<DuplicationReport> checkDocuments(String inputPath, boolean updateLibrary) {
        final JavaPairRDD<Integer, Metadata> shinglesToCheck = this.supplyDocumentShingles(inputPath, true);
        final JavaPairRDD<Integer, Metadata> libShingles = this.retrieveLibrary();

        JavaPairRDD<Integer, Metadata> procSpace = libShingles.union(shinglesToCheck);

        if (updateLibrary) {
            procSpace = procSpace.cache();
            this.updateLibrary(procSpace);
        }

        final JavaPairRDD<Integer, Iterable<Metadata>> groupedShingles = procSpace.groupByKey();

        final JavaPairRDD<Integer, Iterable<Metadata>> filteredGroupedShingles = groupedShingles.filter(groupedShinglesTuple -> {
            final Iterable<Metadata> unfilteredDocMetadata = groupedShinglesTuple._2();
            return StreamSupport.stream(unfilteredDocMetadata.spliterator(), false).anyMatch(Metadata::isMarked);
        });

        // Mapping documents with 1, if in their tuple are neighbors (duplicated shingles), 0 if no. Filtering non-marked documents
        final JavaPairRDD<Metadata, Integer> shingleToOne = filteredGroupedShingles.flatMapToPair(shingleDocMetadataTuple -> {
            final Iterable<Metadata> docMetadata = shingleDocMetadataTuple._2();
            int coincides = (int) StreamSupport.stream(docMetadata.spliterator(), false)
                    .count();
            return StreamSupport.stream(docMetadata.spliterator(), true)
                                .filter(Metadata::isMarked)
                                .map(e -> new Tuple2<>(e, coincides > 1 ? 1 : 0))
                                .collect(Collectors.toList());
        });

        // Reducing, calculating coincidences.
        final JavaPairRDD<Metadata, Integer> shingleCoincides = shingleToOne.reduceByKey((i1, i2) -> i1 + i2);

        // Creating Results, calculating duplication level
        JavaRDD<DuplicationReport> duplicationReports = shingleCoincides.map(shingleCoincidesTuple -> {
            final Metadata documentMetadata = shingleCoincidesTuple._1();
            final Integer coincides = shingleCoincidesTuple._2();
            return new DuplicationReport(documentMetadata, coincides);
        });

        return duplicationReports.collect();
    }

    private JavaSparkContext requireSparkContext() {
        if (this.isSparkContextStopped)
            throw new IllegalStateException("Cannot execute actions on stopped Spark Context");

        if (this.sparkContext == null)
            this.sparkContext = initSparkContext(config);

        return this.sparkContext;
    }

    private JavaPairRDD<Integer, Metadata> supplyDocumentShingles(String inputPath) {
        return this.supplyDocumentShingles(inputPath, false);
    }

    private JavaPairRDD<Integer, Metadata> supplyDocumentShingles(String inputPath, boolean mark) {
        final JavaPairRDD<String, String> textFiles = this.requireSparkContext().wholeTextFiles(Objects.requireNonNull(inputPath));

        final int algShingleSize = config.getShinglesSize();
        final boolean algIsNormalizing = config.isNormalizing();
        if (algShingleSize <= 0)
            throw new PlagioException("Integer size must be bigger than 0!");

        final HashShinglesAlgorithm hashShinglesAlgorithm = new HashShinglesAlgorithm();

        return textFiles.flatMapToPair(textFile -> {
            String fileName = textFile._1().substring(textFile._1().lastIndexOf(File.separator) + 1);
            String content = textFile._2();
            if (algIsNormalizing) {
                content = Texts.cleanFormatting(content);
                content = Texts.removeSpecialCharacters(content);
                content = Texts.removeStopWords(content);
            }

            final List<Integer> textFileShingles = hashShinglesAlgorithm.getHashedShingles(content, algShingleSize);
            final Metadata documentMetadata = new Metadata(fileName, textFileShingles.size(), mark);

            return textFileShingles.stream()
                                   .map(shingle -> new Tuple2<>(shingle, documentMetadata))
                                   .collect(Collectors.toList());
        });
    }

    private JavaPairRDD<Integer, Metadata> retrieveLibrary() {
        try {
            final JavaRDD<Object> rawLibrary = requireSparkContext().objectFile(this.config.getLibraryPath() + "\\*");
            final JavaPairRDD<Integer, Metadata> libShingles = rawLibrary.mapToPair(objectRecord -> (Tuple2<Integer, Metadata>) objectRecord);
            // Fires library retrieving to check if spark will throw InvalidInputException
            libShingles.first();
            return libShingles;
        } catch (Exception e) {
            // Fix InvalidInputException if hadoop finds no cache
            return requireSparkContext().emptyRDD().mapToPair(o -> new Tuple2<>(0, new Metadata("fake", 1)));
        }
    }

    private JavaSparkContext initSparkContext(PlagioConfig cfg) {
        if (cfg.isDebug()) {
            LOGGER.info("Debug mode is ON");
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
            ctx.updateLoggers(conf);
        }

        if (!cfg.isVerbose()) {
            org.apache.log4j.Logger.getLogger("org").setLevel(org.apache.log4j.Level.OFF);
            org.apache.log4j.Logger.getLogger("akka").setLevel(org.apache.log4j.Level.OFF);
        } else {
            LOGGER.info("Verbose mode ON. Spark logging enabled.");
        }

        final String sparkAppName = cfg.getSparkAppName();
        if (sparkAppName == null)
            throw new PlagioException("Spark APP Name cannot be null");

        final String sparkMaster = cfg.getSparkMaster();
        if (sparkMaster == null)
            throw new PlagioException("Spark Master address is null or invalid URI address");

        SparkConf sparkConf = new SparkConf().setAppName(sparkAppName)
                                             .setMaster(sparkMaster);
        return new JavaSparkContext(sparkConf);
    }

    @Override
    public void close() {
        this.sparkContext.close();
        this.isSparkContextStopped = true;
    }
}
