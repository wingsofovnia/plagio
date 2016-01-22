package eu.ioservices.plagio;

import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import eu.ioservices.plagio.model.DuplicationReport;
import eu.ioservices.plagio.model.Metadata;
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

import static eu.ioservices.plagio.algorithm.ShinglesAlgorithm.Shingle;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 27-Dec-15
 */
public class Plagio implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger(Plagio.class);
    private final PlagioConfig config;
    private JavaSparkContext sparkContext;

    public Plagio(PlagioConfig config) {
        this.config = config;
    }

    public void updateLibrary(String inputPath) {
        final JavaPairRDD<Shingle, Metadata> oldShingles = this.retrieveLibrary();
        final JavaPairRDD<Shingle, Metadata> newShingles = this.supplyDocumentShingles(inputPath, true);

        final JavaPairRDD<Shingle, Metadata> procSpace = oldShingles.union(newShingles);
        final JavaPairRDD<Shingle, Metadata> newLibShingles = procSpace.distinct()
                .filter(shingleMetaTuple -> shingleMetaTuple._2().isMarked())
                .mapValues(metadata -> new Metadata(metadata.getDocumentId(), metadata.getTotalShingles()));

        String libOutputPath = config.getLibraryPath() + "/" + System.currentTimeMillis() + "/";
        newLibShingles.saveAsObjectFile(libOutputPath);
    }

    public List<DuplicationReport> checkDocuments(String inputPath) {
        final JavaPairRDD<Shingle, Metadata> shinglesToCheck = this.supplyDocumentShingles(inputPath, true);
        final JavaPairRDD<Shingle, Metadata> libShingles = this.retrieveLibrary();

        final JavaPairRDD<Shingle, Metadata> procSpace = libShingles.union(shinglesToCheck);

        final JavaPairRDD<Shingle, Iterable<Metadata>> groupedShingles = procSpace.groupByKey();

        final JavaPairRDD<Shingle, Iterable<Metadata>> filteredGroupedShingles = groupedShingles.filter(groupedShinglesTuple -> {
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
        if (this.sparkContext == null)
            this.sparkContext = initSparkContext(config);

        return this.sparkContext;
    }

    private JavaPairRDD<Shingle, Metadata> supplyDocumentShingles(String inputPath) {
        return this.supplyDocumentShingles(inputPath, false);
    }

    private JavaPairRDD<Shingle, Metadata> supplyDocumentShingles(String inputPath, boolean mark) {
        final JavaPairRDD<String, String> textFiles = this.requireSparkContext().wholeTextFiles(Objects.requireNonNull(inputPath));

        final int algShingleSize = config.getShinglesSize();
        final boolean algIsNormalizing = config.isNormalizing();
        if (algShingleSize <= 0)
            throw new PlagioException("Shingle size must be bigger than 0!");
        final ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(algIsNormalizing, algShingleSize);

        return textFiles.flatMapToPair(textFile -> {
            String fileName = textFile._1().substring(textFile._1().lastIndexOf(File.separator) + 1);
            String content = textFile._2();
            final List<Shingle> textFileShingles = shinglesAlgorithm.getHashedShingles(content);
            final Metadata documentMetadata = new Metadata(fileName, textFileShingles.size(), mark);

            return textFileShingles.stream()
                                   .map(shingle -> new Tuple2<>(shingle, documentMetadata))
                                   .collect(Collectors.toList());
        });
    }

    private JavaPairRDD<Shingle, Metadata> retrieveLibrary() {
        try {
            final JavaRDD<Object> rawLibrary = requireSparkContext().objectFile(this.config.getLibraryPath() + "\\*");
            final JavaPairRDD<Shingle, Metadata> libShingles = rawLibrary.mapToPair(objectRecord -> (Tuple2<Shingle, Metadata>) objectRecord);
            libShingles.first();
            return libShingles;
        } catch (Exception e) {
            // Fix InvalidInputException if hadoop finds no cache
            return requireSparkContext().emptyRDD().mapToPair(o -> new Tuple2<>(new Shingle(0), new Metadata("fake", 1)));
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
        this.sparkContext = null;
    }
}
