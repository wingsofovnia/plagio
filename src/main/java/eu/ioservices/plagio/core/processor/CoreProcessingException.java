package eu.ioservices.plagio.core.processor;

/**
 * CoreProcessingException is thrown by {@link eu.ioservices.plagio.core.processor.CoreProcessor} implementations
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class CoreProcessingException extends RuntimeException {
    public CoreProcessingException() {
        super();
    }

    public CoreProcessingException(String message) {
        super(message);
    }

    public CoreProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoreProcessingException(Throwable cause) {
        super(cause);
    }

    protected CoreProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
