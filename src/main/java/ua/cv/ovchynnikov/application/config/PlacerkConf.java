package ua.cv.ovchynnikov.application.config;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author superuser
 *         Created 10-Aug-15
 */
public class PlacerkConf implements Serializable {
    public static enum Key {
        APP_CORE("application.core.class"),
        APP_HWCORES("application.hardware.cores"),
        APP_DEBUG("application.mode.debug"),
        APP_VERBOSE("application.mode.verbose"),
        APP_CACHE("application.mode.no_save"),
        APP_IO_CACHE("application.output.cache"),
        APP_IO_INPUT("application.input.documents"),
        APP_ALG_SHINGLE_SIZE("processing.shingles.shingle_size");

        private final String tag;

        Key(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    private final Properties properties;

    public PlacerkConf(String cfgFile) {
        try {
            this.properties = parseProperties(new File(cfgFile));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse property config", e);
        }
    }

    public PlacerkConf(File cfgFile) {
        try {
            this.properties = parseProperties(cfgFile);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse property config", e);
        }
    }

    private Properties parseProperties(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        }
    }

    public Boolean hasProperty(Key prop) {
        String property = this.properties.getProperty(prop.getTag());
        return property != null && property.trim().length() > 0;

    }

    public Property getProperty(Key prop) {
        if (!hasProperty(prop))
            return null;

        return new Property(this.properties.getProperty(prop.getTag()));
    }

    public Property getProperty(Key prop, String defaultValue) {
        if (!hasProperty(prop))
            return new Property(defaultValue);

        if (defaultValue == null)
            return null;
        return new Property(this.properties.getProperty(prop.getTag()));
    }

    public static final class Property {
        private final static String CSV_DELIMITER = ",";
        private final String value;

        public Property(String value) {
            if (value == null)
                throw new IllegalArgumentException("null value in constructor");
            this.value = value;
        }

        public Integer asInt() {
            return Integer.valueOf(this.value);
        }

        public Double asDouble() {
            return Double.valueOf(this.value);
        }

        public Float asFloat() {
            return Float.valueOf(this.value);
        }

        public Boolean asBoolean() {
            return Boolean.valueOf(this.value);
        }

        public String asString() {
            return this.value;
        }

        public List<String> asCSV() {
            String strValue = this.asString();
            if (strValue.contains(CSV_DELIMITER)) {
                String[] split = strValue.split(CSV_DELIMITER);
                return Arrays.stream(split)
                             .map(String::trim)
                             .collect(Collectors.toList());
            } else {
                List<String> lst = new ArrayList<>(1);
                lst.add(strValue.trim());
                return lst;
            }
        }
    }
}
