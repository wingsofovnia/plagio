package eu.ioservices.plagio.processing;

import java.io.Serializable;

/**
 * Created by u548850 on 11/20/2015.
 */
public interface TextProcessor extends Serializable {
    String process(String str);
}
