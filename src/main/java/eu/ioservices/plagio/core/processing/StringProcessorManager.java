package eu.ioservices.plagio.core.processing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by u548850 on 11/20/2015.
 */
public class StringProcessorManager {
    private final List<StringProcessor> stringProcessors;
    private String target;

    public StringProcessorManager() {
        this.stringProcessors = new LinkedList<>();
    }

    public StringProcessorManager addProcessor(StringProcessor stringProcessor) {
        if (stringProcessor == null)
            throw new IllegalArgumentException("Processor is null");
        this.stringProcessors.add(stringProcessor);
        return this;
    }

    public StringProcessorManager removeProcessor(StringProcessor stringProcessor) {
        if (stringProcessor == null)
            throw new IllegalArgumentException("Processor is null");
        this.stringProcessors.remove(stringProcessor);
        return this;
    }

    public StringProcessorManager removeProcessor(Class cls) {
        if (cls == null)
            throw new IllegalArgumentException("Class obj is null");
        Iterator<StringProcessor> processorIterator = this.stringProcessors.iterator();
        while (processorIterator.hasNext()) {
            StringProcessor p = processorIterator.next();
            if (p.getClass().equals(cls))
                processorIterator.remove();
        }
        return this;
    }

    public List<StringProcessor> getStringProcessors() {
        return stringProcessors;
    }

    public StringProcessorManager setTarget(String target) {
        this.target = target;
        return this;
    }

    public String flush() {
        String str = this.target;
        for (StringProcessor p : getStringProcessors()) {
            str = p.process(str);
        }
        return str;
    }
}
