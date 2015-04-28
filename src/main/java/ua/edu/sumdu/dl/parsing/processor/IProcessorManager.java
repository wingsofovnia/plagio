package ua.edu.sumdu.dl.parsing.processor;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 26-Apr-15
 */
public interface IProcessorManager<T> extends Serializable {
    IProcessorManager<T> addProcessor(IProcessor<T> processor);

    IProcessorManager<T> removeProcessor(IProcessor<T> processor);

    IProcessorManager<T> removeProcessor(Class cls);

    IProcessorManager<T> setTarget(T target);

    T flush();
}
