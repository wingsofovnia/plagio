package eu.ioservices.plagio;

import eu.ioservices.plagio.config.FileBasedConfig;
import eu.ioservices.plagio.core.publishing.ConsoleResultPublisher;
import eu.ioservices.plagio.core.processing.SimpleCoreProcessor;

/**
 * Entry point of Plagio application.
 * <br/>
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class Application {
    public static void main(String[] args) {
        final FileBasedConfig fileBasedConfig = new FileBasedConfig();
        fileBasedConfig.setDebug(true);
        fileBasedConfig.setInputPath("TEST/doc/");
        fileBasedConfig.setCachePath("TEST/cache/");

        Plagio.builder()
                .config(fileBasedConfig)
                .core(new SimpleCoreProcessor())
                .publisher(new ConsoleResultPublisher())
                .build()
                .process();
    }
}
