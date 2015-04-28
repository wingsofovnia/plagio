package ua.edu.sumdu.dl.parsing.processor;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 28-Mar-15
 */
public interface IProcessor<T> extends Serializable {
    public T process(T str);
}
