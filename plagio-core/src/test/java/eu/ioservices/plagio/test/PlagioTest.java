package eu.ioservices.plagio.test;

import eu.ioservices.plagio.Plagio;
import eu.ioservices.plagio.PlagioConfig;
import eu.ioservices.plagio.model.DuplicationReport;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class PlagioTest {
    private static final int ALLOWED_DUPLICATION_LEVEL_DEVIATION = 3;
    private static final int TEST_LIB_EXPECTED_DUPLICATION_LEVEL = 35;
    private static final int TEST_EMPTY_LIB_EXPECTED_DUPLICATION_LEVEL = 0;
    private static final String FILE_PART_ONE = "file1.txt";
    private static final String FILE_PART_ONE_PLUS_ADDITIONS = "file1partPlus.txt";
    private static final String TEMP_LIB_DIR = System.getProperty("java.io.tmpdir") + File.separator + "cache";
    private static final String TEMP_INPUT_DIR = System.getProperty("java.io.tmpdir") + File.separator + "input";
    private static final PlagioConfig PLAGIO_CONFIG = new PlagioConfig() {{
        setDebug(true);
        setVerbose(false);
        setSparkMaster("local[4]");
        setSparkAppName("PlatioTest");
        setLibraryPath(TEMP_LIB_DIR);
        setShinglesSize(5);
    }};

    @BeforeClass
    public static void createEnv() throws IOException {
        final File libTemp = new File(TEMP_LIB_DIR);
        final File inputTemp = new File(TEMP_INPUT_DIR);

        FileUtils.deleteDirectory(libTemp);
        FileUtils.deleteDirectory(inputTemp);

        libTemp.mkdirs();
        inputTemp.mkdirs();
    }

    @AfterClass
    public static void destroyEnv() throws IOException {
        final File libTemp = new File(TEMP_LIB_DIR);
        final File inputTemp = new File(TEMP_INPUT_DIR);

        FileUtils.deleteDirectory(libTemp);
        FileUtils.deleteDirectory(inputTemp);
    }

    private void cleanEnv() throws IOException {
        final File libTemp = new File(TEMP_LIB_DIR);
        final File inputTemp = new File(TEMP_INPUT_DIR);

        FileUtils.cleanDirectory(libTemp);
        FileUtils.cleanDirectory(inputTemp);
    }

    @Test
    public void testEmptyLib() throws IOException {
        cleanEnv();
        System.out.println("        -> testEmptyLib() test");
        final URL filePartOneResource = Thread.currentThread().getContextClassLoader().getResource(FILE_PART_ONE);
        File dest = new File(TEMP_INPUT_DIR + File.separator + FILE_PART_ONE);
        FileUtils.copyURLToFile(filePartOneResource, dest);

        try (final Plagio plagio = new Plagio(PLAGIO_CONFIG)) {
            final List<DuplicationReport> duplicationReports = plagio.checkDocuments(TEMP_INPUT_DIR);
            System.out.println("  -- " + duplicationReports.toString());
            assertTrue(inaccurateEquals(duplicationReports.get(0).getDuplicationLevel(), TEST_EMPTY_LIB_EXPECTED_DUPLICATION_LEVEL));
        }
    }

    @Test
    public void testLibrary() throws IOException {
        cleanEnv();
        System.out.println("        -> testLibrary() test");
        final URL filePartOneResource = Thread.currentThread().getContextClassLoader().getResource(FILE_PART_ONE);
        File destInputPartOne = new File(TEMP_INPUT_DIR + File.separator + FILE_PART_ONE);
        FileUtils.copyURLToFile(filePartOneResource, destInputPartOne);

        try (final Plagio plagio = new Plagio(PLAGIO_CONFIG)) {
            plagio.updateLibrary(TEMP_INPUT_DIR);
        }

        FileUtils.cleanDirectory(new File(TEMP_INPUT_DIR));

        final URL filePartOnePlusResource = Thread.currentThread().getContextClassLoader().getResource(FILE_PART_ONE_PLUS_ADDITIONS);
        File destInputPartOnePlus = new File(TEMP_INPUT_DIR + File.separator + FILE_PART_ONE_PLUS_ADDITIONS);
        FileUtils.copyURLToFile(filePartOnePlusResource, destInputPartOnePlus);

        try (final Plagio plagio = new Plagio(PLAGIO_CONFIG)) {
            final List<DuplicationReport> duplicationReports = plagio.checkDocuments(TEMP_INPUT_DIR);
            System.out.println("  -- " + duplicationReports.toString());
            assertTrue(inaccurateEquals(duplicationReports.get(0).getDuplicationLevel(), TEST_LIB_EXPECTED_DUPLICATION_LEVEL));
        }
    }

    boolean inaccurateEquals(double actual, int excepted) {
        return actual <= excepted + ALLOWED_DUPLICATION_LEVEL_DEVIATION && actual >= excepted - ALLOWED_DUPLICATION_LEVEL_DEVIATION;
    }
}
