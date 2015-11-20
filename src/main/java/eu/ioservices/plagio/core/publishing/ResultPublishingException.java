package eu.ioservices.plagio.core.publishing;

/**
 * CoreProcessingException is thrown by {@link eu.ioservices.plagio.core.publishing.ResultPublisher} implementations
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class ResultPublishingException extends RuntimeException {
    public ResultPublishingException() {
        super();
    }

    public ResultPublishingException(String message) {
        super(message);
    }

    public ResultPublishingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultPublishingException(Throwable cause) {
        super(cause);
    }

    protected ResultPublishingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
