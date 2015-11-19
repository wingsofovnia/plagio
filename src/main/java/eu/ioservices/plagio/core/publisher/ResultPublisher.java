package eu.ioservices.plagio.core.publisher;

import eu.ioservices.plagio.model.Result;

import java.util.Collection;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 19-Nov-15
 */
public interface ResultPublisher {
    void publish(Result result) throws ResultPublishingException ;
    void publish(Collection<Result> results) throws ResultPublishingException ;
}
