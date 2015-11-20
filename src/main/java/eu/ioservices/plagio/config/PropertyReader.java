package eu.ioservices.plagio.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Plagio configuration that loads properties from *.properties file by it's name or {@link java.io.File} object
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class PropertyReader extends Properties {
    private static final String CSV_DELIMITER = ",";

    public void load(String file) {
        try {
            load(new FileInputStream(file));
        } catch (IOException e) {
            throw new ConfigException(e);
        }
    }


    public <T> T getProperty(String key, Class<T> clazz) {
        final String property = getProperty(key);
        if (property == null)
            return null;

        return parse(property, clazz);
    }


    public <T> T getProperty(String key, T defaultValue, Class<T> clazz) {
        final String property = getProperty(key, String.valueOf(defaultValue));
        if (property == null)
            return null;

        return parse(property, clazz);
    }


    public String getRequiredProperty(String key) {
        final String property = getProperty(key);
        if (property == null)
            throw new ConfigException("Missing mandatory field: " + key);

        return property;
    }


    public <T> T getRequiredProperty(String key, Class<T> clazz) {
        return parse(getRequiredProperty(key), clazz);
    }


    public String getRequiredProperty(String key, String defaultValue) {
        try {
            return getRequiredProperty(key);
        } catch (ConfigException e) {
            if (defaultValue == null) {
                throw new ConfigException("Required prop and default value are null");
            } else {
                return defaultValue;
            }
        }
    }


    public <T> T getRequiredProperty(String key, T defaultValue, Class<T> clazz) {
        return parse(getRequiredProperty(key, String.valueOf(defaultValue)), clazz);
    }

    public Properties slice(String prefix) {
        final Properties slice = new PropertyReader();

        this.stringPropertyNames()
                .stream()
                .filter(pn -> pn.startsWith(prefix))
                .map(pn -> pn.substring(prefix.length()))
                .forEach(pn -> slice.put(pn, this.get(pn)));

        return slice;
    }

    public List<String> getRequiredCsvProperty(String key) {
        final String value = this.getRequiredProperty(key);

        if (value.contains(CSV_DELIMITER)) {
            String[] split = value.split(CSV_DELIMITER);
            return Arrays.stream(split)
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            List<String> lst = new ArrayList<>(1);
            lst.add(value.trim());
            return lst;
        }
    }

    public List<String> getCsvProperty(String key) {
        try {
            return getRequiredCsvProperty(key);
        } catch (ConfigException e) {
            return Collections.emptyList();
        }
    }

    private <T> T parse(String str, Class<T> clazz) {
        if (clazz.equals(Integer.class))
            return clazz.cast(Integer.valueOf(str));
        else if (clazz.equals(Boolean.class))
            return clazz.cast(Boolean.valueOf(str));
        else if (clazz.equals(Float.class))
            return clazz.cast(Float.valueOf(str));
        else if (clazz.equals(Double.class))
            return clazz.cast(Double.valueOf(str));
        else {
            throw new ConfigException("Failed to parse string, unknown parser for " + clazz.getCanonicalName());
        }
    }

}
