package ua.edu.sumdu.dl.spark;

/**
 * @author superuser
 *         Created 19-Apr-15
 */
public class PlachertException extends RuntimeException {
    public PlachertException() {
        super();
    }

    public PlachertException(String message) {
        super(message);
    }

    public PlachertException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlachertException(Throwable cause) {
        super(cause);
    }

    protected PlachertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
