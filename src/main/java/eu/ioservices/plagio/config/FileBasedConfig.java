package eu.ioservices.plagio.config;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 20-Nov-15
 */

public class FileBasedConfig implements Config {
    private boolean debug;
    private boolean verbose;
    private boolean caching;

    private String inputPath;
    private String cachePath;

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getCachePath() {
        return cachePath;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean isCaching() {
        return caching;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }
}
