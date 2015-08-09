package ua.edu.sumdu.dl;

import com.sanityinc.jargs.CmdLineParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import scala.Tuple2;
import ua.edu.sumdu.dl.algorithm.ShinglesAlgorithm;
import ua.edu.sumdu.dl.parsing.ConverterException;
import ua.edu.sumdu.dl.parsing.IConverter;
import ua.edu.sumdu.dl.parsing.TikaConverter;
import ua.edu.sumdu.dl.pojo.DocumentMeta;
import ua.edu.sumdu.dl.pojo.Result;
import ua.edu.sumdu.dl.spark.PlachertConf;
import ua.edu.sumdu.dl.spark.PlachertContext;
import ua.edu.sumdu.dl.spark.db.JdbcRddSupplier;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * @author superuser
 *         Created 27-Mar-15
 */
public class PlachertSparkDriver implements Serializable {
    private static final String SPARK_APP_NAME = "ua.edu.sumdu.dl.antiplagiarism";
    private static final Logger LOGGER = LogManager.getLogger(PlachertSparkDriver.class);

    private static void printUsage() {
        System.err.println(
                "Usage: PlachertSparkDriver [--debug] - enable debug logging \n" +
                        "[--config] - path to config properties file" +
                        "[--output] - results output file \n" +
                        "[--silent] - disable results output \n" +
                        "[--verbose] - enable spark logging \n" +
                        "[--no-save] - do not save new documents' shingles into database \n" +
                        "[--documents] - documents id as CVS for IN query (10,20,44,22..)");
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // [Init] Parsing input parameters
        CmdLineParser argsParser = new CmdLineParser();
        CmdLineParser.Option<Boolean> debug = argsParser.addBooleanOption("debug");
        CmdLineParser.Option<Boolean> silent = argsParser.addBooleanOption("silent");
        CmdLineParser.Option<Boolean> verbose = argsParser.addBooleanOption("verbose");
        CmdLineParser.Option<Boolean> noSave = argsParser.addBooleanOption("no-save");
        CmdLineParser.Option<String> configFile = argsParser.addStringOption("config");
        CmdLineParser.Option<String> output = argsParser.addStringOption("output");
        CmdLineParser.Option<String> docIds = argsParser.addStringOption("documents");
        try {
            argsParser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }

        Boolean isDebugEnabled = argsParser.getOptionValue(debug, false);
        Boolean isSilentMode = argsParser.getOptionValue(silent, false);
        Boolean isVerboseEnabled = argsParser.getOptionValue(verbose, false);
        Boolean isNoSaveMode = argsParser.getOptionValue(noSave, false);
        String configPropFile = argsParser.getOptionValue(configFile, null);
        String outputResultsFile = argsParser.getOptionValue(output, null);
        String documentsIds = argsParser.getOptionValue(docIds, null);

        // [Init] Building Conf object, overriding values from config file
        PlachertConf plachertConf;
        if (configPropFile != null)
            plachertConf = new PlachertConf(configPropFile);
        else
            plachertConf = new PlachertConf();

        if (isDebugEnabled)
            plachertConf.setDebug(true);
        if (plachertConf.isDebug()) {
            LOGGER.info("Debug mode is ON");
            plachertConf.setDebug(true);
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
            ctx.updateLoggers(conf);
        }

        if (isSilentMode)
            plachertConf.setSilent(true);
        if (plachertConf.isSilent())
            LOGGER.warn("Silent mode is ENABLED. No result output will be given.");

        if (outputResultsFile != null)
            plachertConf.setOutputFile(outputResultsFile);
        if (plachertConf.isOutputFile())
            LOGGER.info("Output to {} file ENABLED.", plachertConf.getOutputFile());

        if (documentsIds != null)
            plachertConf.setDocumentIds(documentsIds);
        if (!plachertConf.isDocumentIds())
            LOGGER.warn("No documents' ids argument has been found. ALL documents will be processed.");

        if (isNoSaveMode)
            plachertConf.setNoSave(true);
        if (plachertConf.isNoSave())
            LOGGER.warn("No-save mode is ENABLED. New documents' shingles into database WONT be saved!");


        if (isVerboseEnabled)
            plachertConf.setVerbose(true);
        if (!plachertConf.isVerbose()) {
            org.apache.log4j.Logger.getLogger("org").setLevel(org.apache.log4j.Level.OFF);
            org.apache.log4j.Logger.getLogger("akka").setLevel(org.apache.log4j.Level.OFF);
        } else {
            LOGGER.info("Verbose mode ON. Spark logging disabled.");
        }

        // [Init] Building contexts
        LOGGER.info("Creating Spark Context, running Spark driver ...");
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName(SPARK_APP_NAME);
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        LOGGER.info("Spark Context has been successfully created.");

        LOGGER.info("Creating AntiPlagiarism Context ...");
        PlachertContext ac = new PlachertContext(plachertConf);
        LOGGER.info("AntiPlagiarism context has been successfully created.");

        // [#D1] Building supplier object for documents
        String docTable = plachertConf.getDocumentsTable();
        String docPk = plachertConf.getDocumentsPrimaryKey();
        String docName = plachertConf.getDocumentsTitleColumn();
        String docCont = plachertConf.getDocumentsContentColumn();
        JdbcRddSupplier documentJdbcRddSupplier = new JdbcRddSupplier(sc, ac).primaryKey(docPk);

        if (plachertConf.isDocumentIds()) {
            String docInIds = plachertConf.getDocumentIds();
            documentJdbcRddSupplier.from("(SELECT " + docPk + ", "
                                                    + docName + ", "
                                                    + docCont + " FROM " + docTable
                                                              + " WHERE " + docPk
                                                                          + " IN (" + docInIds + ")) as docs");
        } else {
            documentJdbcRddSupplier.from("(SELECT " + docPk + ", "
                                                    + docName + ", "
                                                    + docCont + " FROM " + docTable + ")) as docs");
        }

        // [#D2] Processing documents with shingles algorithm and remapping
        JavaRDD<Row> documentDataSupply = documentJdbcRddSupplier.supply();

        JavaPairRDD<Integer, DocumentMeta> newShingleDocumentMetaPairRDD = documentDataSupply.flatMapToPair(row -> {
            int documentId = row.getInt(0);
            String documentName = row.getString(1);
            byte[] rawContent = row.<byte[]>getAs(2);
            try {
                IConverter<InputStream, String> textConverter = new TikaConverter(ac.getStringProcessManager());
                String content = textConverter.parse(new ByteArrayInputStream(rawContent));

                ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(content, plachertConf.getShinglesAlgorithmShingleSize());
                Set<Integer> hashedShingles = shinglesAlgorithm.getDistinctHashedShingles();

                List<Tuple2<Integer, DocumentMeta>> shingleDocumentMetaTuplesList = new LinkedList<>();
                for (Integer shingle : hashedShingles) {
                    shingleDocumentMetaTuplesList.add(new Tuple2<>(shingle, new DocumentMeta(documentId, documentName, true, hashedShingles.size())));
                }

                return shingleDocumentMetaTuplesList;
            } catch (ConverterException e) {
                LOGGER.error("Failed to parse document #{}, with documentName {}", documentId, documentName);
            }
            return null;
        });

        // [#S1] Building supplier object for old shingles
        JdbcRddSupplier shingesJdbcRddSupplier = new JdbcRddSupplier(sc, ac);
        String shglPk = plachertConf.getShinglesPrimaryKey();
        String shgTable = plachertConf.getShinglesTable();
        String shgCnt = plachertConf.getShinglesContentColumn();
        shingesJdbcRddSupplier.from("(SELECT " + shglPk + ", "
                                               + shgCnt + " FROM " + shgTable
                                                                   + ") AS SNH")
                              .primaryKey(shglPk)
                              .calculateBounds();

        JavaRDD<Row> shingesDataSupply = shingesJdbcRddSupplier.supply();

        // [#S2] Supplying old shingles to Spark system
        JavaPairRDD<Integer, DocumentMeta> oldShinglesRDD = shingesDataSupply.mapToPair(row -> new Tuple2<>(row.getInt(2), new DocumentMeta(row.getInt(1))));

        // [#2] Uniting old shingles and newly created from documents RDDs'
        JavaPairRDD<Integer, DocumentMeta> unitedShingleDocumentMetaPairRDD = oldShinglesRDD.union(newShingleDocumentMetaPairRDD);

        // [#3] Grouping documents with same shingles
        JavaPairRDD<Integer, Iterable<DocumentMeta>> groupedShingleDocumentMetasPairRDD = unitedShingleDocumentMetaPairRDD.groupByKey();

        // [#4] Filtering tuples that don’t have tag of the document we are checking (marked)
        JavaPairRDD<Integer, Iterable<DocumentMeta>> filteredShingleDocumentMetasPairRDD = groupedShingleDocumentMetasPairRDD.filter(shingleDocumentMetaTuple -> {
            Iterable<DocumentMeta> unfilteredDocMetas = shingleDocumentMetaTuple._2();
            return StreamSupport.stream(unfilteredDocMetas.spliterator(), false).anyMatch(DocumentMeta::isMarked);
        });

        // [#5] Saving new shingles into database
        if (!plachertConf.isNoSave()) {
            LOGGER.info("Saving new shingles into database");
            filteredShingleDocumentMetasPairRDD.foreach(shingleDocumentsMetaDataTuple -> {
                Iterable<DocumentMeta> documentMetas = shingleDocumentsMetaDataTuple._2();
                int size = ((Collection<?>) documentMetas).size();
                if (size == 1) {
                    try (Connection connection = ac.buildJdbcConnection()) {
                        Integer shingleHash = shingleDocumentsMetaDataTuple._1();
                        int documentId = documentMetas.iterator().next().getDocumentId();

                        try {
                            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + plachertConf.getShinglesTable() + " (document_id, hash) VALUES (?, ?)");
                            insertStatement.setInt(1, documentId);
                            insertStatement.setInt(2, shingleHash);
                            insertStatement.execute();
                        } catch (SQLException e) {
                            LOGGER.error("Failed to save shingle: {}", e.getMessage());
                        }
                    }
                }
            });
        } else {
            LOGGER.info("No-save mode is ENABLED, no documents' shingles will be saved.");
        }

        // [#6] Mapping documents with 1, if in their tuple are neighbors (duplicated shingles), 0 if no. Filtering non-marked documents
        JavaPairRDD<DocumentMeta, Integer> documentMetaCoincidesPairRDD = filteredShingleDocumentMetasPairRDD.flatMapToPair(shingleDocumentsMetaDataTuple -> {
            List<Tuple2<DocumentMeta, Integer>> documentsMetaCoincidencesTupleList = new ArrayList<>();
            Iterable<DocumentMeta> documentsMetadata = shingleDocumentsMetaDataTuple._2();

            int coincides = ((Collection<?>) documentsMetadata).size();
            for (DocumentMeta metadata : documentsMetadata) {
                if (!metadata.isMarked())
                    continue;
                documentsMetaCoincidencesTupleList.add(new Tuple2<>(metadata, coincides > 1 ? 1 : 0));
            }

            return documentsMetaCoincidencesTupleList;
        });

        // [#7] Reducing, calculating coincidences.
        JavaPairRDD<DocumentMeta, Integer> reducedDocumentMetaCoincidesPairRDD = documentMetaCoincidesPairRDD.reduceByKey((i1, i2) -> i1 + i2);

        // [#8] Creating Results, calculating duplication level
        JavaRDD<Result> resultsRDD = reducedDocumentMetaCoincidesPairRDD.map(documentMetadataCoincidencesTuple ->
                new Result(documentMetadataCoincidencesTuple._1(), documentMetadataCoincidencesTuple._2()));

        List<Result> results = resultsRDD.collect();

        if (!plachertConf.isSilent()) {
            if (results.size() == 0) {
                LOGGER.error("No results has been generated! Output missed.");
            } else {
                StringBuilder outputBuilder = new StringBuilder();
                outputBuilder.append("#-------------------------------------------# \n");
                for (Result r : results) {
                    DocumentMeta metadata = r.getMetadata();
                    outputBuilder.append("  -> Document #").append(metadata.getDocumentId()).append(" (").append(metadata.getName()).append(") \n");
                    outputBuilder.append("     Total shingles: ").append(metadata.getShinglesAmount()).append("\n");
                    outputBuilder.append("     Coincides: ").append(r.getCoincidences()).append("\n");
                    outputBuilder.append("     PLAGIARISM LEVEL: ").append((int) r.getDuplicationLevel()).append("% \n");
                    outputBuilder.append("\n");
                }
                outputBuilder.deleteCharAt(outputBuilder.length() - 1);
                outputBuilder.append("#-------------------------------------------# \n");

                PrintStream outputStream = null;
                if (plachertConf.isOutputFile()) {
                    try {
                        outputStream = new PrintStream(new FileOutputStream(plachertConf.getOutputFile()));
                        LOGGER.debug("Created stream for result output for {} file", outputResultsFile);
                    } catch (FileNotFoundException e) {
                        LOGGER.error("Failed to create/open output file {}", outputResultsFile, e);
                    }
                } else {
                    outputStream = new PrintStream(System.out);
                    LOGGER.debug("No output file has been specified. Using default System.out.");
                }

                if (outputStream != null)
                    try (PrintStream out = outputStream) {
                        out.print(outputBuilder.toString());
                    }
            }
        }
    }
}