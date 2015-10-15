package eu.ioservices.plagio.core;

import eu.ioservices.plagio.config.AppConfiguration;
import eu.ioservices.plagio.model.Result;

import java.util.List;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public interface CoreProcessor {
    List<Result> process(final AppConfiguration appConfiguration);
}
