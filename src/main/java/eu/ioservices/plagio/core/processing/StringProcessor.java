package eu.ioservices.plagio.core.processing;

import java.io.Serializable;

/**
 * Created by u548850 on 11/20/2015.
 */
public interface StringProcessor extends Serializable {
    String process(String str);
}
