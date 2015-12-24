package eu.ioservices.plagio.processing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by u548850 on 11/20/2015.
 */
public class TextProcessorManager {
    private final List<TextProcessor> stringProcessors;
    private String target;

    public TextProcessorManager() {
        this.stringProcessors = new LinkedList<>();
    }

    public TextProcessorManager addProcessor(TextProcessor stringProcessor) {
        if (stringProcessor == null)
            throw new IllegalArgumentException("Processor is null");
        this.stringProcessors.add(stringProcessor);
        return this;
    }

    public TextProcessorManager removeProcessor(TextProcessor stringProcessor) {
        if (stringProcessor == null)
            throw new IllegalArgumentException("Processor is null");
        this.stringProcessors.remove(stringProcessor);
        return this;
    }

    public TextProcessorManager removeProcessor(Class cls) {
        if (cls == null)
            throw new IllegalArgumentException("Class obj is null");
        Iterator<TextProcessor> processorIterator = this.stringProcessors.iterator();
        while (processorIterator.hasNext()) {
            TextProcessor p = processorIterator.next();
            if (p.getClass().equals(cls))
                processorIterator.remove();
        }
        return this;
    }

    public List<TextProcessor> getStringProcessors() {
        return stringProcessors;
    }

    public TextProcessorManager setTarget(String target) {
        this.target = target;
        return this;
    }

    public String flush() {
        String str = this.target;
        for (TextProcessor p : getStringProcessors()) {
            str = p.process(str);
        }
        return str;
    }
}
