package eu.ioservices.plagio.core.processing;

import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import eu.ioservices.plagio.config.Config;
import eu.ioservices.plagio.config.FileBasedConfig;
import eu.ioservices.plagio.model.Meta;
import eu.ioservices.plagio.model.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple {@link CoreProcessor} implementation that uses {@link eu.ioservices.plagio.algorithm.ShinglesAlgorithm}
 * for determining documents' duplication level on the local computer
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class SimpleCoreProcessor implements CoreProcessor {
    private static final Logger LOGGER = LogManager.getLogger(SimpleCoreProcessor.class);
    private Map<Meta, Iterable<Integer>> dataStore = new HashMap<>();

    @Override
    public List<Result> process(Config config) {
        if (!(config instanceof FileBasedConfig))
            throw new CoreException("SimpleCoreProcessor requires FileBasedConfig");
        FileBasedConfig plagioConf = (FileBasedConfig) config;

        final String inputDir = plagioConf.getInputPath();
        final String[] inputFileNames = new File(inputDir).list();

        try {
            for (String inputFileName : inputFileNames) {
                final byte[] rawInputFileContent = Files.readAllBytes(Paths.get(inputDir + inputFileName));
                final String inputFileContent = new String(rawInputFileContent, "UTF-8");

                final ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(inputFileContent);
                final List<Integer> shingles = shinglesAlgorithm.getHashedShingles();

                dataStore.put(new Meta(inputFileName, shingles.size()), shingles);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file", e);
            throw new CoreException(e);
        }

        List<Result> results = new ArrayList<>(dataStore.size());
        for (Meta fileMeta : dataStore.keySet()) {
            int coincides = 0;

            final Iterable<Integer> fileShingles = dataStore.get(fileMeta);
            for (Integer shingle : fileShingles) {
                coincides += countDocShingleCoincides(fileMeta, shingle);
            }

            results.add(new Result(fileMeta, coincides));
        }

        return results;
    }

    private int countDocShingleCoincides(Meta doc, Integer shingle) {
        int coincides = 0;

        for (Meta fileMeta : dataStore.keySet()) {
            if (doc.equals(fileMeta))
                continue;

            final Iterable<Integer> fileShingles = dataStore.get(fileMeta);

            for (Integer fileShingle : fileShingles)
                if (fileShingle.equals(shingle))
                    coincides++;
        }

        return coincides;
    }
}
