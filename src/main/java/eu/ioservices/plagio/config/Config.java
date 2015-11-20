package eu.ioservices.plagio.config;

import eu.ioservices.plagio.core.converting.Converter;
import eu.ioservices.plagio.core.processing.StringProcessorManager;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 *         Created 20-Nov-15
 */
public interface Config {
    boolean isDebug();

    boolean isVerbose();

    boolean isCaching();

    Converter converter();
    void converter(Converter converter);

    StringProcessorManager stringProcessorManager();
    void stringProcessorManager(StringProcessorManager converter);
}