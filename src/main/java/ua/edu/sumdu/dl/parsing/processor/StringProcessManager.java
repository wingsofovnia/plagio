package ua.edu.sumdu.dl.parsing.processor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author superuser
 *         Created 26-Apr-15
 */
public class StringProcessManager implements IProcessorManager<String> {
    private String target;
    private final List<IProcessor<String>> processors;

    public StringProcessManager() {
        processors = new LinkedList<>();
    }

    public StringProcessManager(List<IProcessor<String>> processors) {
        this.processors = processors;
    }

    @Override
    public StringProcessManager addProcessor(IProcessor<String> processor) {
        if (processor == null)
            throw new IllegalArgumentException("Processor is null");
        processors.add(processor);
        return this;
    }

    @Override
    public StringProcessManager removeProcessor(IProcessor<String> processor) {
        if (processor == null)
            throw new IllegalArgumentException("Processor is null");
        processors.remove(processor);
        return this;
    }

    @Override
    public StringProcessManager removeProcessor(Class cls) {
        if (cls == null)
            throw new IllegalArgumentException("Class obj is null");
        Iterator<IProcessor<String>> processorIterator = processors.iterator();
        while (processorIterator.hasNext()) {
            IProcessor p = processorIterator.next();
            if (p.getClass().equals(cls))
                processorIterator.remove();
        }
        return this;
    }

    @Override
    public StringProcessManager setTarget(String target) {
        this.target = target;
        return this;
    }

    @Override
    public String flush() {
        String str = target;
        for (IProcessor<String> p : processors) {
            str = p.process(str);
        }
        return str;
    }
}