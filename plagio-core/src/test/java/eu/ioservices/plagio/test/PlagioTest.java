package eu.ioservices.plagio.test;

import eu.ioservices.plagio.Plagio;
import eu.ioservices.plagio.PlagioConfig;
import eu.ioservices.plagio.model.DuplicationReport;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class PlagioTest {
    private static final double DUPLICATION_LEVEL_PRECISION = 1;
    private static final double EXPECTED_TEST_LIB_DUPLICATION_LEVEL = 40;
    private static final int EXPECTED_TEST_LIB_COINCIDES_NUM = 6;
    private static final int EXPECTED_TEST_LIB_PLAG_SHINGLES_NUM = 15;
    private static final double EXPECTED_TEST_EMPTY_LIB_DUPLICATION_LEVEL = 0;
    private static final int EXPECTED_TEST_EMPTY_LIB_ORIGIN_SHINGLES_NUM = 19;

    private static final String ORIGINAL_TEXT_FILENAME = "orig.txt";
    private static final String PLAGIARISM_FILENAME = "plag.txt";

    private static final String TEMP_LIB_DIR_PATH = System.getProperty("java.io.tmpdir") + File.separator + "lib";
    private static final String TEMP_INPUT_DIR_PATH = System.getProperty("java.io.tmpdir") + File.separator + "input";

    private static final PlagioConfig PLAGIO_CONFIG = new PlagioConfig() {{
        setDebug(true);
        setVerbose(false);
        setSparkMaster("local[4]");
        setSparkAppName("PlatioTest");
        setLibraryPath(TEMP_LIB_DIR_PATH);
    }};

    @Before
    public void createEnv() throws IOException {
        new File(TEMP_LIB_DIR_PATH).mkdirs();
        new File(TEMP_INPUT_DIR_PATH).mkdirs();
    }

    @After
    public void destroyEnv() throws IOException {
        FileUtils.deleteDirectory(new File(TEMP_LIB_DIR_PATH));
        FileUtils.deleteDirectory(new File(TEMP_INPUT_DIR_PATH));
    }

    @Test
    public void testEmptyLib() throws IOException {
        System.out.println("        -> testEmptyLib() test");
        final URL filePartOneResource = Thread.currentThread().getContextClassLoader().getResource(ORIGINAL_TEXT_FILENAME);
        File dest = new File(TEMP_INPUT_DIR_PATH + File.separator + ORIGINAL_TEXT_FILENAME);
        FileUtils.copyURLToFile(filePartOneResource, dest);

        try (final Plagio plagio = new Plagio(PLAGIO_CONFIG)) {
            final List<DuplicationReport> duplicationReports = plagio.checkDocuments(TEMP_INPUT_DIR_PATH);
            System.out.println("  -- " + duplicationReports.toString());
            assertEquals(EXPECTED_TEST_EMPTY_LIB_DUPLICATION_LEVEL, duplicationReports.get(0).getDuplicationLevel(), DUPLICATION_LEVEL_PRECISION);
            assertEquals(EXPECTED_TEST_EMPTY_LIB_ORIGIN_SHINGLES_NUM, duplicationReports.get(0).getMetadata().getTotalShingles(), DUPLICATION_LEVEL_PRECISION);
        }
    }

    @Test
    public void testLibrary() throws IOException {
        System.out.println("        -> testLibrary() test");
        final URL originalDocResource = Thread.currentThread().getContextClassLoader().getResource(ORIGINAL_TEXT_FILENAME);
        File tempDestOriginalDocResource = new File(TEMP_INPUT_DIR_PATH + File.separator + ORIGINAL_TEXT_FILENAME);
        FileUtils.copyURLToFile(originalDocResource, tempDestOriginalDocResource);

        try (final Plagio plagio = new Plagio(PLAGIO_CONFIG)) {
            plagio.updateLibrary(TEMP_INPUT_DIR_PATH);
        }

        FileUtils.cleanDirectory(new File(TEMP_INPUT_DIR_PATH));

        final URL filePartOnePlusResource = Thread.currentThread().getContextClassLoader().getResource(PLAGIARISM_FILENAME);
        File destInputPartOnePlus = new File(TEMP_INPUT_DIR_PATH + File.separator + PLAGIARISM_FILENAME);
        FileUtils.copyURLToFile(filePartOnePlusResource, destInputPartOnePlus);

        try (final Plagio plagio = new Plagio(PLAGIO_CONFIG)) {
            final List<DuplicationReport> duplicationReports = plagio.checkDocuments(TEMP_INPUT_DIR_PATH);
            System.out.println("  -- " + duplicationReports.toString());
            assertEquals(EXPECTED_TEST_LIB_DUPLICATION_LEVEL, duplicationReports.get(0).getDuplicationLevel(), DUPLICATION_LEVEL_PRECISION);
            assertEquals(EXPECTED_TEST_LIB_PLAG_SHINGLES_NUM, duplicationReports.get(0).getMetadata().getTotalShingles(), DUPLICATION_LEVEL_PRECISION);
            assertEquals(EXPECTED_TEST_LIB_COINCIDES_NUM, duplicationReports.get(0).getDocCoincidences(), DUPLICATION_LEVEL_PRECISION);
        }
    }

}
