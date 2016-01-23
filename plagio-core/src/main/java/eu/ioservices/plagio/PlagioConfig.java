package eu.ioservices.plagio;

import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 27-Dec-15
 */
public class PlagioConfig {
    private static final String DEFAULT_SPARK_APP_NAME = "PlagioDriver";
    private String sparkMaster;
    private String sparkAppName = DEFAULT_SPARK_APP_NAME;

    private boolean isDebug;
    private boolean isVerbose;

    private String libraryPath;

    private int shinglesSize = ShinglesAlgorithm.DEFAULT_SHINGLE_SIZE;
    private boolean isNormalizing = ShinglesAlgorithm.DEFAULT_NORMALIZING_SWITCH;

    public int getShinglesSize() {
        return shinglesSize;
    }

    public void setShinglesSize(int shinglesSize) {
        this.shinglesSize = shinglesSize;
    }

    public boolean isNormalizing() {
        return isNormalizing;
    }

    public void setNormalizing(boolean isNormalizing) {
        this.isNormalizing = isNormalizing;
    }

    public String getSparkMaster() {
        return sparkMaster;
    }

    public PlagioConfig setSparkMaster(String sparkMaster) {
        this.sparkMaster = sparkMaster;
        return this;
    }

    public String getSparkAppName() {
        return sparkAppName;
    }

    public PlagioConfig setSparkAppName(String sparkAppName) {
        this.sparkAppName = sparkAppName;
        return this;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public PlagioConfig setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public PlagioConfig setVerbose(boolean isVerbose) {
        this.isVerbose = isVerbose;
        return this;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public PlagioConfig setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
        return this;
    }
}
