package eu.ioservices.plagio;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 27-Dec-15
 */
public class PlagioConfig {
    private String sparkMaster;
    private String sparkAppName;

    private boolean isDebug;
    private boolean isVerbose;

    private String libraryPath;

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
