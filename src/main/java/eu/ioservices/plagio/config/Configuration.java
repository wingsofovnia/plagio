package eu.ioservices.plagio.config;

import java.util.Objects;
import java.util.Properties;

/**
 * Created by u548850 on 11/20/2015.
 */
public class Configuration extends Properties {
    /**
     * Creates an empty configuration object with no values.
     */
    public Configuration() {
        super();
    }

    /**
     * Creates an empty configuration object with the specified defaults.
     */
    public Configuration(Properties defaults) {
        super(defaults);
    }

    /**
     * Searches for the property with the specified key in this Configuration object.
     * Works the same way {@link Properties#getProperty(String)} works but parses String value into appropriate type
     *
     * @param key the hashtable key.
     * @param type the class object of the type you want return value to be
     * @param <T> the class of the object you want return value to be
     *
     * @return casted to T and parsed value in this property list with the specified key value.
     */
    public <T> T getProperty(String key, Class<T> type) {
        return parse(super.getProperty(key).trim(), type);
    }

    /**
     * Searches for the property with the specified key in this Configuration object and returns default value if there
     * is no value under specified key
     * Works the same way {@link Properties#getProperty(String)} works but parses String value into appropriate type
     *
     * @param key the hashtable key.
     * @param defaultValue a default value.
     * @param type the class object of the type you want return value to be
     * @param <T> the class of the object you want return value to be
     *
     * @return casted to T and parsed value in this property list with the specified key value or default value
     */
    public <T> T getProperty(String key, T defaultValue, Class<T> type) {
        if (!super.containsKey(Objects.requireNonNull(key)))
            return defaultValue;

        return this.getProperty(key, type);
    }

    /**
     * Searches for the property with the specified key in this Configuration object.
     * Works the same way {@link Properties#getProperty(String)} works, but if the key is not found or value,
     * situated under this key, is empty, method throws ConfigurationException
     *
     * @param key the hashtable key.
     *
     * @return not-null and not-empty sting value in this property list with the specified key value.
     *
     * @throws ConfigurationException if the key is not found or value, situated under this key, is empty
     */
    public String getRequiredProperty(String key) throws ConfigurationException {
        return this.getRequiredProperty(key, String.class);
    }

    /**
     * Searches for the property with the specified key in this Configuration object and casts String value to specified
     * type.
     * Works the same way {@link Configuration#getProperty(String, Class)} works, but if the key is not found or value,
     * situated under this key, is empty, method throws ConfigurationException
     *
     * @param key the hashtable key.
     * @param type the class object of the type you want return value to be
     * @param <T> the class of the object you want return value to be
     *
     * @return not-null, not-empty, casted to T and parsed value in this property list with the specified key value.
     *
     * @throws ConfigurationException
     */
    public <T> T getRequiredProperty(String key, Class<T> type) throws ConfigurationException {
        if (!super.containsKey(key))
            throw new ConfigurationException("There is no value under key = " + key);

        String propValue = super.getProperty(key).trim();
        if (propValue.length() == 0)
            throw new ConfigurationException("Value for this key is empty. Key = " + key);

        return parse(propValue, type);
    }

    /**
     * Casts and parses string value to specified type
     *
     * @param prop string to parse
     * @param type the class object of the type you want return value to be
     * @param <T> the class of the object you want return value to be
     *
     * @return parsed and casted value
     */
    protected <T> T parse(String prop, Class<T> type) {
        try {
            if (Boolean.class.equals(type)) {
                return (T) (Boolean) (prop.equals("1") || Boolean.valueOf(prop));
            } else if (Integer.class.equals(type)) {
                return (T) Integer.valueOf(prop);
            } else if (Double.class.equals(type)) {
                return (T) Double.valueOf(prop);
            } else if (Float.class.equals(type)) {
                return (T) Float.valueOf(prop);
            } else if (Long.class.equals(type)) {
                return (T) Long.valueOf(prop);
            } else if (Byte.class.equals(type)) {
                return (T) Byte.valueOf(prop);
            } else if (Short.class.equals(type)) {
                return (T) Short.valueOf(prop);
            } else if (String.class.equals(type)) {
                return (T) prop;
            } else {
                throw new ConfigurationException("Failed to cast property value = '" + prop + "'");
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Failed to parse string", e);
        }
    }
}
