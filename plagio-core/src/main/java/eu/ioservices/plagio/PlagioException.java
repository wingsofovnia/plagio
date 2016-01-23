package eu.ioservices.plagio;

/**
 * CoreProcessingException is thrown by {@link CoreProcessor} implementations
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class PlagioException extends RuntimeException {
    public PlagioException() {
        super();
    }

    public PlagioException(String message) {
        super(message);
    }

    public PlagioException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlagioException(Throwable cause) {
        super(cause);
    }

    protected PlagioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
