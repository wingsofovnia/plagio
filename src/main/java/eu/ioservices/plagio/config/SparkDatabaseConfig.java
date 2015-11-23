package eu.ioservices.plagio.config;

/**
 * Created by u548850 on 11/23/2015.
 */
public class SparkDatabaseConfig extends Config {
    private String driverName;
    private String connectionUrl;
    private String userName;
    private String password;
    private String docTableName;
    private String docTablePKColumn;
    private String docTableContentColumn;
    private String cacheTableName;
    private String cacheTablePKColumn;
    private String cacheTableContentColumn;
    private int lowerBound;
    private int upperBound;
    private int numPartitions;

    public String getCacheTableContentColumn() {
        return cacheTableContentColumn;
    }

    public void setCacheTableContentColumn(String cacheTableContentColumn) {
        this.cacheTableContentColumn = cacheTableContentColumn;
    }

    public String getCacheTableName() {
        return cacheTableName;
    }

    public void setCacheTableName(String cacheTableName) {
        this.cacheTableName = cacheTableName;
    }

    public String getCacheTablePKColumn() {
        return cacheTablePKColumn;
    }

    public void setCacheTablePKColumn(String cacheTablePKColumn) {
        this.cacheTablePKColumn = cacheTablePKColumn;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getDocTableContentColumn() {
        return docTableContentColumn;
    }

    public void setDocTableContentColumn(String docTableContentColumn) {
        this.docTableContentColumn = docTableContentColumn;
    }

    public String getDocTableName() {
        return docTableName;
    }

    public void setDocTableName(String docTableName) {
        this.docTableName = docTableName;
    }

    public String getDocTablePKColumn() {
        return docTablePKColumn;
    }

    public void setDocTablePKColumn(String docTablePKColumn) {
        this.docTablePKColumn = docTablePKColumn;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public int getNumPartitions() {
        return numPartitions;
    }

    public void setNumPartitions(int numPartitions) {
        this.numPartitions = numPartitions;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
