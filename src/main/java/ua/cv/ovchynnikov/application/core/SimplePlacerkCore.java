package ua.cv.ovchynnikov.application.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.cv.ovchynnikov.application.PlacerkCore;
import ua.cv.ovchynnikov.application.config.PlacerkConf;
import ua.cv.ovchynnikov.pojo.Meta;
import ua.cv.ovchynnikov.pojo.Result;
import ua.cv.ovchynnikov.processing.algorithm.ShinglesAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author superuser
 *         Created 09-Sep-15
 */
public class SimplePlacerkCore implements PlacerkCore {
    private static final Logger LOGGER = LogManager.getLogger(SimplePlacerkCore.class);
    private Map<Meta, Iterable<Integer>> dataStore = new HashMap<>();

    @Override
    public List<Result> run(PlacerkConf appConf) {
        final String inputDir = appConf.getProperty(PlacerkConf.Key.APP_IO_INPUT).asString();
        final String[] inputFileNames = new File(inputDir).list();

        try {
            for (String inputFileName : inputFileNames) {
                final byte[] rawInputFileContent = Files.readAllBytes(Paths.get(inputDir + inputFileName));
                final String inputFileContent = new String(rawInputFileContent, "UTF-8");

                final ShinglesAlgorithm shinglesAlgorithm = new ShinglesAlgorithm(inputFileContent, appConf.getProperty(PlacerkConf.Key.APP_ALG_SHINGLE_SIZE).asInt());
                final List<Integer> shingles = shinglesAlgorithm.getHashedShingles();

                dataStore.put(new Meta(inputFileName, shingles.size()), shingles);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file", e);
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
