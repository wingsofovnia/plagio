package eu.ioservices.plagio.converting;

/**
 * @author superuser
 *         Created 28-Mar-15
 */
public class TextConverterException extends RuntimeException {
    public TextConverterException() {
        super();
    }

    public TextConverterException(String message) {
        super(message);
    }

    public TextConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public TextConverterException(Throwable cause) {
        super(cause);
    }

    protected TextConverterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
