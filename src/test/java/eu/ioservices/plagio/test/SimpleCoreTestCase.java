package eu.ioservices.plagio.test;

import eu.ioservices.plagio.Plagio;
import eu.ioservices.plagio.config.FileBasedConfig;
import eu.ioservices.plagio.core.SimpleCoreProcessor;
import eu.ioservices.plagio.core.converting.TikaConverter;
import eu.ioservices.plagio.core.processing.NormalizingStringProcessor;
import eu.ioservices.plagio.core.processing.StringProcessorManager;
import eu.ioservices.plagio.model.Result;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * This test covers using of {@link SimpleCoreProcessor} with 3 files, where third of them consists of 55% of the first
 * one and of 35% of the second one.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class SimpleCoreTestCase {
    private static final double DOUBLE_DELTA = 3;
    private static final String TEST_INPUT_PATH = "src/test/resources/";
    private static final Map<String, Double> RESULTS_FILE_TO_PLAGIARISM_TUPLE = new HashMap<String, Double>() {{
        put("test-file.txt", 55.0);
        put("test-file2.txt", 35.0);
        put("test-file1_2.txt", 100.0);
    }};

    @Test
    public void test() {
        final FileBasedConfig fileBasedConfig = new FileBasedConfig();
        fileBasedConfig.setDebug(true);
        fileBasedConfig.setInputPath(TEST_INPUT_PATH);

        List<Result> results = Plagio.builder()
                .config(fileBasedConfig)
                .core(new SimpleCoreProcessor())
                .stringProcessorManager(new StringProcessorManager() {{
                    addProcessor(new NormalizingStringProcessor());
                }})
                .converter(new TikaConverter())
                .build()
                .process();
        results.stream().map(Result::toString).forEach(System.out::println);

        results.stream().forEach(result -> {
            String doc = result.getDocName();
            double actualPlagiarism = result.getDuplicationLevel();
            double expectedPlagiarism = RESULTS_FILE_TO_PLAGIARISM_TUPLE.get(doc);

            assertEquals(expectedPlagiarism, actualPlagiarism, DOUBLE_DELTA);
        });
    }
}
