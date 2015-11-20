package eu.ioservices.plagio.config;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 20-Nov-15
 */
public class SparkFileBasedConfig extends FileBasedConfig {
    private static final int DEFAULT_HW_CORES = 4;
    private static final String DEFAULT_SPARK_APP_NAME = "plagio_spark_file_core";
    private static final String DEFAULT_SPARK_APP_MASTER = "localhost";

    private String sparkAppName = DEFAULT_SPARK_APP_NAME;
    private String sparkAppMaster = DEFAULT_SPARK_APP_MASTER;
    private int hwCores = DEFAULT_HW_CORES;

    public String getSparkAppName() {
        return sparkAppName;
    }

    public void setSparkAppName(String sparkAppName) {
        this.sparkAppName = sparkAppName;
    }

    public String getSparkAppMaster() {
        return sparkAppMaster;
    }

    public void setSparkAppMaster(String sparkAppMaster) {
        this.sparkAppMaster = sparkAppMaster;
    }

    public int getHwCores() {
        return hwCores;
    }

    public void setHwCores(int hwCores) {
        this.hwCores = hwCores;
    }
}
