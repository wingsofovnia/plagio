package ua.edu.sumdu.dl.spark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.edu.sumdu.dl.parsing.processor.IProcessor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author superuser
 *         Created 26-Apr-15
 */
public class PlachertConf implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(PlachertConf.class);
    private static final String DEFAULT_CONFIG_FILE_PATH = "config.properties";

    private static final int DEFAULT_CLUSTER_CORES = 4;
    private int clusterCores;

    private static final String DEFAULT_DATABASE_DRIVER_NAME = "com.mysql.jdbc.Driver";
    private String databaseDriverName;
    private String databaseConnectionUrl;
    private String databaseUsername;
    private String databasePassword;

    private static final String DEFAULT_DOCUMENT_TABLE = "document";
    private String documentsTable;
    private static final String DEFAULT_DOCUMENT_PRIMARY_KEY = "document_id";
    private String documentsPrimaryKey;

    private static final String DEFAULT_SHINGLE_TABLE = "shingle";
    private String shinglesTable;
    private static final String DEFAULT_SHINGLE_PRIMARY_KEY = "document_id";
    private String shinglesPrimaryKey;

    private List<IProcessor<String>> parsingProcessors = new ArrayList<>();

    private static final int DEFAULT_SHINGLES_ALGORITHM_SHINGLE_SIZE = 4;
    private int shinglesAlgorithmShingleSize;

    private boolean isDebug = false;
    private boolean isSilent = false;
    private boolean verbose = false;
    private boolean noSave = false;

    private String outputFile;
    private String documentIds;

    public PlachertConf() {
        this(DEFAULT_CONFIG_FILE_PATH);
    }

    public PlachertConf(InputStream stream) {
        readFromPropertyFile(stream);
    }

    public PlachertConf(boolean parse) {
        if (parse)
            try {
                readFromPropertyFile(new FileInputStream(DEFAULT_CONFIG_FILE_PATH));
            } catch (FileNotFoundException e) {
                throw new PlachertInitializationException("Failed to open stream", e);
            }
    }

    public PlachertConf(String fileName) {
        try {
            readFromPropertyFile(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            throw new PlachertInitializationException("Failed to open stream", e);
        }
    }

    public int getClusterCores() {
        return clusterCores;
    }

    public PlachertConf setClusterCores(int clusterCores) {
        this.clusterCores = clusterCores;
        return this;
    }

    public String getDatabaseDriverName() {
        return databaseDriverName;
    }

    public PlachertConf setDatabaseDriverName(String databaseDriverName) {
        this.databaseDriverName = databaseDriverName;
        return this;
    }

    public String getDatabaseConnectionUrl() {
        return databaseConnectionUrl;
    }

    public PlachertConf setDatabaseConnectionUrl(String databaseConnectionUrl) {
        this.databaseConnectionUrl = databaseConnectionUrl;
        return this;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public PlachertConf setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
        return this;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public PlachertConf setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
        return this;
    }

    public String getDocumentsTable() {
        return documentsTable;
    }

    public PlachertConf setDocumentsTable(String documentsTable) {
        this.documentsTable = documentsTable;
        return this;
    }

    public String getDocumentsPrimaryKey() {
        return documentsPrimaryKey;
    }

    public PlachertConf setDocumentsPrimaryKey(String documentsPrimaryKey) {
        this.documentsPrimaryKey = documentsPrimaryKey;
        return this;
    }

    public String getShinglesTable() {
        return shinglesTable;
    }

    public PlachertConf setShinglesTable(String shinglesTable) {
        this.shinglesTable = shinglesTable;
        return this;
    }

    public String getShinglesPrimaryKey() {
        return shinglesPrimaryKey;
    }

    public PlachertConf setShinglesPrimaryKey(String shinglesPrimaryKey) {
        this.shinglesPrimaryKey = shinglesPrimaryKey;
        return this;
    }

    public List<IProcessor<String>> getParsingProcessors() {
        return parsingProcessors;
    }

    public PlachertConf setParsingProcessors(List<IProcessor<String>> parsingProcessors) {
        if (parsingProcessors == null)
            throw new IllegalArgumentException("parsingProcessors null");
        this.parsingProcessors = parsingProcessors;
        return this;
    }

    public PlachertConf addParsingProcessors(IProcessor<String> parsingProcessor) {
        if (parsingProcessor == null)
            throw new IllegalArgumentException("parsingProcessor null");
        this.parsingProcessors.add(parsingProcessor);
        return this;
    }

    public int getShinglesAlgorithmShingleSize() {
        return shinglesAlgorithmShingleSize;
    }

    public PlachertConf setShinglesAlgorithmShingleSize(int shinglesAlgorithmShingleSize) {
        this.shinglesAlgorithmShingleSize = shinglesAlgorithmShingleSize;
        return this;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public PlachertConf setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public PlachertConf setSilent(boolean isSilent) {
        this.isSilent = isSilent;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public PlachertConf setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isNoSave() {
        return noSave;
    }

    public PlachertConf setNoSave(boolean noSave) {
        this.noSave = noSave;
        return this;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isOutputFile() {
        return this.outputFile != null;
    }

    public PlachertConf setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public boolean isDocumentIds() {
        return this.documentIds != null;
    }

    public String getDocumentIds() {
        return documentIds;
    }

    public PlachertConf setDocumentIds(String documentIds) {
        this.documentIds = documentIds;
        return this;
    }

    private void readFromPropertyFile(InputStream stream) {
        try (InputStream inputStream = stream) {
            Properties properties = new Properties();
            properties.load(inputStream);
            LOGGER.info("Config file has been loaded successfully!");

            String cores = properties.getProperty("spark.global.cores");
            if (cores != null) {
                setClusterCores(Integer.parseInt(cores));
            } else {
                LOGGER.warn("No cluster cores amount set in configuration, using default value {}", DEFAULT_CLUSTER_CORES);
                setClusterCores(DEFAULT_CLUSTER_CORES);
            }

            String driver = properties.getProperty("spark.db.supply.connection.driver");
            if (driver != null) {
                setDatabaseDriverName(driver);
            } else {
                LOGGER.warn("No sql driver set in configuration, using default value {}", DEFAULT_DATABASE_DRIVER_NAME);
                setDatabaseDriverName(DEFAULT_DATABASE_DRIVER_NAME);
            }

            String connectionUrl = properties.getProperty("spark.db.supply.connection.url");
            if (connectionUrl != null)
                setDatabaseConnectionUrl(connectionUrl);
            else
                throw new PlachertInitializationException("Failed to get connection url from config file");

            String connectionUsername = properties.getProperty("spark.db.supply.connection.username");
            if (connectionUsername != null)
                setDatabaseUsername(connectionUsername);
            else
                throw new PlachertInitializationException("Failed to get connection username from config file");

            String connectionPassword = properties.getProperty("spark.db.supply.connection.password");
            if (connectionPassword != null)
                setDatabasePassword(connectionPassword);
            else
                throw new PlachertInitializationException("Failed to get connection password from config file");

            String docTable = properties.getProperty("database.supply.document.table");
            if (docTable != null) {
                setDocumentsTable(docTable);
            } else {
                LOGGER.warn("No document table name set in configuration, using default value {}", DEFAULT_DOCUMENT_TABLE);
                setDocumentsTable(DEFAULT_DOCUMENT_TABLE);
            }

            String docKey = properties.getProperty("database.supply.document.primary_key");
            if (docKey != null) {
                setDocumentsPrimaryKey(docKey);
            } else {
                LOGGER.warn("No documents' table's primary key set in configuration, using default value {}", DEFAULT_DOCUMENT_PRIMARY_KEY);
                setDocumentsPrimaryKey(DEFAULT_DOCUMENT_PRIMARY_KEY);
            }

            String shingleTable = properties.getProperty("database.supply.shingle.table");
            if (shingleTable != null) {
                setShinglesTable(shingleTable);
            } else {
                LOGGER.warn("No shingle table name set in configuration, using default value {}", DEFAULT_SHINGLE_TABLE);
                setShinglesTable(DEFAULT_SHINGLE_TABLE);
            }

            String shingleKey = properties.getProperty("database.supply.shingle.primary_key");
            if (shingleKey != null) {
                setShinglesPrimaryKey(shingleKey);
            } else {
                LOGGER.warn("No shingles' table's primary key set in configuration, using default value {}", DEFAULT_SHINGLE_PRIMARY_KEY);
                setShinglesPrimaryKey(DEFAULT_SHINGLE_PRIMARY_KEY);
            }

            String debugMode = properties.getProperty("spark.application.debug");
            if (debugMode != null)
                setDebug(Boolean.parseBoolean(debugMode));

            String silentMode = properties.getProperty("spark.application.silent");
            if (silentMode != null)
                setSilent(Boolean.parseBoolean(silentMode));

            String verboseMode = properties.getProperty("spark.application.verbose");
            if (verboseMode != null)
                setVerbose(Boolean.parseBoolean(verboseMode));

            String noSaveMode = properties.getProperty("spark.application.no_save");
            if (noSaveMode != null)
                setNoSave(Boolean.parseBoolean(noSaveMode));

            String outputFile = properties.getProperty("spark.application.output");
            if (outputFile != null && outputFile.length() > 0)
                setOutputFile(outputFile);

            String docIds = properties.getProperty("spark.application.documents");
            if (docIds != null && outputFile.length() > 0)
                setDocumentIds(docIds);

            String stringProcessors = properties.getProperty("parsing.processors");
            if (stringProcessors != null && stringProcessors.length() > 0) {
                String[] processors = stringProcessors.split("\\s*,\\s*");
                for (String processorClassName : processors) {
                    try {
                        Class<?> processorClass = Class.forName(processorClassName);
                        Object processor = processorClass.getConstructors()[0].newInstance();
                        parsingProcessors.add((IProcessor<String>) processor);
                        LOGGER.info("String processor {} has been loaded", processorClassName);
                    } catch (Exception e) {
                        throw new PlachertInitializationException("Failed to create string processors", e);
                    }
                }
                LOGGER.info("Total processors loaded: " + parsingProcessors.size());
            } else {
                LOGGER.warn("No parsing processors has been loaded");
            }

            String algShinglesAmount = properties.getProperty("algorithm.shingles.shingle_size");
            if (algShinglesAmount != null) {
                setShinglesAlgorithmShingleSize(Integer.parseInt(algShinglesAmount));
            } else {
                LOGGER.warn("No shingle algorithm shingle size value has been found in file, using default {}", DEFAULT_SHINGLES_ALGORITHM_SHINGLE_SIZE);
                setShinglesAlgorithmShingleSize(DEFAULT_SHINGLES_ALGORITHM_SHINGLE_SIZE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading config  file.");
        }
    }
}
