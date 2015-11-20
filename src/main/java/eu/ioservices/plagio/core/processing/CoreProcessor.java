package eu.ioservices.plagio.core.processing;

import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.model.Result;

import java.util.List;

/**
 * CoreProcessor represents an engine, that is used by Plagio application for near-duplicate document determining.
 * <br/>
 * This interface decouples Plagio {@link eu.ioservices.plagio.Application} from algorithms of finding documents'
 * duplication level.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public interface CoreProcessor {
    List<Result> process(Config config) throws CoreException;
}
