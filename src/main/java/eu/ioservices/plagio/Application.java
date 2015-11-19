package eu.ioservices.plagio;

import eu.ioservices.plagio.config.AppConfiguration;
import eu.ioservices.plagio.core.output.ConsoleResultPublisher;
import eu.ioservices.plagio.core.output.ResultPublisher;
import eu.ioservices.plagio.core.processor.CoreProcessor;
import eu.ioservices.plagio.model.Result;

import java.util.List;

import static eu.ioservices.plagio.config.AppConfiguration.Key;

/**
 * Entry point of Plagio application.
 * <br/>
 * Here Plagio configures itself, determine {@link eu.ioservices.plagio.core.processor.CoreProcessor} implementation and delegate
 * further processing to underling core implementation.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class Application {
    private static final String DEFAULT_APP_CONFIG = "config.properties";
    private static final String DEFAULT_APP_CORE = "ua.cv.ovchynnikov.apos.application.core.spark.SparkPlacerkCore";

    public static void main(String[] args) {
        final AppConfiguration cfg = new AppConfiguration(args.length > 0 ? args[0] : DEFAULT_APP_CONFIG);

        final String appClass = cfg.getProperty(Key.APP_CORE, DEFAULT_APP_CORE).asString();
        CoreProcessor placerkApp;
        try {
            placerkApp = (CoreProcessor) Class.forName(appClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Placerk Core", e);
        }

        List<Result> results = placerkApp.process(cfg);

        final Boolean printResults = cfg.getProperty(Key.APP_IO_RESULTS_PRINT, "false").asBoolean();

        if (printResults) {
            ResultPublisher resultPublisher = new ConsoleResultPublisher();
            resultPublisher.publish(results);
        }
    }
}
