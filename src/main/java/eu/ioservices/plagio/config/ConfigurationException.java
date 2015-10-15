package eu.ioservices.plagio.config;

import eu.ioservices.plagio.ApplicationException;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public class ConfigurationException extends ApplicationException {
    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    protected ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
