package ua.cv.ovchynnikov;

import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author superuser
 *         Created 11-Aug-15
 */
public class TestAll {
    private static final String CONFIG_FILE = "/config.test.properties";

    private static final String INPUT_DIR_RESOURCE = "/samples";
    private static final String INPUT_DIR = "temp" + File.separator;

    @Before
    public void copyConfig() throws Exception {
        InputStream inputStream = TestAll.class.getResourceAsStream(CONFIG_FILE);
        OutputStream outputStream = new FileOutputStream(new File(CONFIG_FILE));

        int read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        inputStream.close();
        outputStream.close();
    }

   // @Test
    public void s() {

    }
/*
    @Before
    public void createTextSamples() throws Exception {
        Path samples = Paths.get(TestAll.class.getResource(INPUT_DIR_RESOURCE).toURI());
        Path inputDir = Paths.get(INPUT_DIR);
        Files.copy(samples, inputDir.resolve(samples.getFileName()));
    }

    @Test
    public void run() throws Exception {
        PlachertSparkDriver.main(new String[]{CONFIG_FILE});
    }

    @After
    public void removeConfig() throws Exception {
        Files.deleteIfExists(Paths.get(CONFIG_FILE));
    }

    @After
    public void removeTextSamples() throws Exception {
        Files.deleteIfExists(Paths.get(INPUT_DIR));
    }*/
}
