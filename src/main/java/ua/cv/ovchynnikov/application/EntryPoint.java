package ua.cv.ovchynnikov.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.cv.ovchynnikov.application.config.PlacerkConf;
import ua.cv.ovchynnikov.pojo.Result;

import java.io.PrintStream;
import java.util.List;

import static ua.cv.ovchynnikov.application.config.PlacerkConf.Key;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public class EntryPoint {
    private static final Logger LOGGER = LogManager.getLogger(EntryPoint.class);
    private static final String APP_CONFIG = "config.properties";
    private static final String DEFAULT_APP_CORE = "ua.cv.ovchynnikov.application.spark.SparkPlacerkApplication";

    public static void main(String[] args) {
        final PlacerkConf cfg = new PlacerkConf(args.length > 0 ? args[0] : APP_CONFIG);

        final String appClass = cfg.getProperty(Key.APP_CORE, DEFAULT_APP_CORE).asString();
        PlacerkApplication placerkApp;
        try {
            placerkApp = (PlacerkApplication) Class.forName(appClass).newInstance();
        } catch (Exception e) {
            throw new ApplicationException("Failed to init Placerk Core", e);
        }

        List<Result> results = placerkApp.run(cfg);

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
