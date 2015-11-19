package eu.ioservices.plagio.algorithm;

/**
 * AlgorithmException is thrown by algorithm implementations
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class AlgorithmException extends RuntimeException {
    public AlgorithmException() {
        super();
    }

    public AlgorithmException(String message) {
        super(message);
    }

    public AlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlgorithmException(Throwable cause) {
        super(cause);
    }

    protected AlgorithmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
