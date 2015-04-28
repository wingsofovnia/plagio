package ua.edu.sumdu.dl.spark;

/**
 * @author superuser
 *         Created 19-Apr-15
 */
public class PlachertInitializationException extends PlachertException {
    public PlachertInitializationException() {
    }

    public PlachertInitializationException(String message) {
        super(message);
    }

    public PlachertInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlachertInitializationException(Throwable cause) {
        super(cause);
    }

    public PlachertInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
