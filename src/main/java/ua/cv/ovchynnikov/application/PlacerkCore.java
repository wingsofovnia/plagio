package ua.cv.ovchynnikov.application;

import ua.cv.ovchynnikov.application.config.PlacerkConf;
import ua.cv.ovchynnikov.pojo.Result;

import java.util.List;

/**
 * @author superuser
 *         Created 13-Aug-15
 */
public interface PlacerkCore {
    List<Result> run(final PlacerkConf appConf);
}
