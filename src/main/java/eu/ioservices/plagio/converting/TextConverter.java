package eu.ioservices.plagio.converting;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by u548850 on 11/20/2015.
 */
public interface TextConverter extends Serializable {
    String convert(InputStream target);
}
