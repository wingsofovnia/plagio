package eu.ioservices.plagio.config;

import eu.ioservices.plagio.converting.TextConverter;
import eu.ioservices.plagio.processing.TextProcessorManager;

/**
 * Created by u548850 on 11/20/2015.
 */
public class Config {
    private static final int DEFAULT_HW_CORES = 4;
    private static final String DEFAULT_SPARK_APP_NAME = "plagio_spark_file_core";
    private static final String DEFAULT_SPARK_APP_MASTER = "localhost";

    private String sparkAppName = DEFAULT_SPARK_APP_NAME;
    private String sparkAppMaster = DEFAULT_SPARK_APP_MASTER;
    private int hwCores = DEFAULT_HW_CORES;
    private boolean debug;
    private boolean verbose;
    private boolean caching;

    private TextConverter converter;
    private TextProcessorManager stringProcessorManager;

    public void converter(TextConverter converter) {
        this.converter = converter;
    }

    public int getHwCores() {
        return hwCores;
    }

    public void setHwCores(int hwCores) {
        this.hwCores = hwCores;
    }

    public String getSparkAppMaster() {
        return sparkAppMaster;
    }

    public void setSparkAppMaster(String sparkAppMaster) {
        this.sparkAppMaster = sparkAppMaster;
    }

    public String getSparkAppName() {
        return sparkAppName;
    }

    public void setSparkAppName(String sparkAppName) {
        this.sparkAppName = sparkAppName;
    }

    public boolean isCaching() {
        return caching;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public TextConverter getConverter() {
        return converter;
    }

    public void setConverter(TextConverter converter) {
        this.converter = converter;
    }

    public TextProcessorManager getStringProcessorManager() {
        return stringProcessorManager;
    }

    public void setStringProcessorManager(TextProcessorManager stringProcessorManager) {
        this.stringProcessorManager = stringProcessorManager;
    }
}
