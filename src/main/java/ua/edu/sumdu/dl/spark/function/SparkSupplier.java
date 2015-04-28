package ua.edu.sumdu.dl.spark.function;

import scala.runtime.AbstractFunction0;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 23-Apr-15
 */
public abstract class SparkSupplier<T> extends AbstractFunction0<T> implements Serializable {
}
