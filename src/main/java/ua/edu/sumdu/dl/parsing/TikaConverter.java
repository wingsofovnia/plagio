package ua.edu.sumdu.dl.parsing;

import org.apache.tika.Tika;
import ua.edu.sumdu.dl.parsing.processor.StringProcessManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author superuser
 *         Created 20-Apr-15
 */
public class TikaConverter implements IConverter<InputStream, String> {
    private final StringProcessManager stringProcessManager;

    public TikaConverter(StringProcessManager stringProcessManager) {
        this.stringProcessManager = stringProcessManager;
    }

    @Override
    public String parse(InputStream target) {
        if (target == null)
            throw new IllegalArgumentException("Input stream is null");

        Tika tika = new Tika();
        try {
            return stringProcessManager.setTarget(tika.parseToString(target)).flush();
        } catch (Exception e) {
            throw new ConverterException(e);
        } finally {
            try {
                target.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
