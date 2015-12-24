package eu.ioservices.plagio.converting;

import org.apache.tika.Tika;

import java.io.InputStream;
import java.util.Objects;

/**
 * Created by u548850 on 11/20/2015.
 */
public class TikaConverter implements TextConverter {

    @Override
    public String convert(InputStream target) {
        Objects.requireNonNull(target);

        Tika tika = new Tika();
        try {
            return tika.parseToString(target);
        } catch (Exception e) {
            throw new TextConverterException(e);
        }
    }
}
