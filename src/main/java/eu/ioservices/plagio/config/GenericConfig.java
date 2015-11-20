package eu.ioservices.plagio.config;

import eu.ioservices.plagio.core.converting.Converter;
import eu.ioservices.plagio.core.processing.StringProcessorManager;

import java.util.Scanner;

/**
 * Created by u548850 on 11/20/2015.
 */
public class GenericConfig implements Config {
    private boolean debug;
    private boolean verbose;
    private boolean caching;

    private Converter converter = target -> new Scanner(target).useDelimiter("\\A").next();
    private StringProcessorManager stringProcessorManager = new StringProcessorManager();

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean isCaching() {
        return caching;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    @Override
    public Converter converter() {
        return this.converter;
    }

    @Override
    public void converter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public StringProcessorManager stringProcessorManager() {
        return this.stringProcessorManager;
    }

    @Override
    public void stringProcessorManager(StringProcessorManager stringProcessorManager) {
        this.stringProcessorManager = stringProcessorManager;
    }
}
