package eu.ioservices.plagio.core.converting;

import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by u548850 on 11/20/2015.
 */
public class TikaConverter implements Converter {
    @Override
    public String parse(InputStream target) {
        if (target == null)
            throw new IllegalArgumentException("Input stream is null");

        Tika tika = new Tika();
        try {
            return tika.parseToString(target);
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
