package eu.ioservices.plagio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.ioservices.plagio.config.AppConfiguration;
import eu.ioservices.plagio.core.CoreProcessor;
import eu.ioservices.plagio.model.Result;

import java.io.PrintStream;
import java.util.List;

import static eu.ioservices.plagio.config.AppConfiguration.Key;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public class EntryPoint {
    private static final Logger LOGGER = LogManager.getLogger(EntryPoint.class);
    private static final String DEFAULT_APP_CONFIG = "config.properties";
    private static final String DEFAULT_APP_CORE = "ua.cv.ovchynnikov.apos.application.core.spark.SparkPlacerkCore";

    public static void main(String[] args) {
        final AppConfiguration cfg = new AppConfiguration(args.length > 0 ? args[0] : DEFAULT_APP_CONFIG);

        final String appClass = cfg.getProperty(Key.APP_CORE, DEFAULT_APP_CORE).asString();
        CoreProcessor placerkApp;
        try {
            placerkApp = (CoreProcessor) Class.forName(appClass).newInstance();
        } catch (Exception e) {
            throw new ApplicationException("Failed to init Placerk Core", e);
        }

        List<Result> results = placerkApp.process(cfg);

        if (results.size() == 0) {
            LOGGER.error("No results has been generated! Output missed.");
        } else {
            StringBuilder outputBuilder = new StringBuilder();
            outputBuilder.append("#-------------------------------------------# \n");
            for (Result r : results) {
                outputBuilder.append("  -> Document #").append(r.getDocName()).append("\n");
                outputBuilder.append("     Total shingles: ").append(r.getDocShingles()).append("\n");
                outputBuilder.append("     Coincides: ").append(r.getCoincidences()).append("\n");
                outputBuilder.append("     PLAGIARISM LEVEL: ").append((int) r.getDuplicationLevel()).append("% \n");
                outputBuilder.append("\n");
            }
            outputBuilder.deleteCharAt(outputBuilder.length() - 1);
            outputBuilder.append("#-------------------------------------------# \n");

            try (PrintStream out = new PrintStream(System.out)) {
                out.print(outputBuilder.toString());
            }
        }
    }
}
