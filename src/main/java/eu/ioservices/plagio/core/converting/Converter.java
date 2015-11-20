package eu.ioservices.plagio.core.converting;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by u548850 on 11/20/2015.
 */
public interface Converter extends Serializable {
    String parse(InputStream target);
}
