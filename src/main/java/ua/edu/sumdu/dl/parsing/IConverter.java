package ua.edu.sumdu.dl.parsing;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 20-Apr-15
 */
public interface IConverter<T, E> extends Serializable {
    E parse (T target);
}
