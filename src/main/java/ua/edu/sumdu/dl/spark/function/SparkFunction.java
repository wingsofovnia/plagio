package ua.edu.sumdu.dl.spark.function;

import scala.runtime.AbstractFunction1;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 23-Apr-15
 */
public abstract class SparkFunction<T, E> extends AbstractFunction1<T, E> implements Serializable {
}
