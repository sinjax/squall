package org.openimaj.util.function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A source defines the operation of an empty function.
 * A function whose inputs are ignored (or rather are null)
 * and produce an output 
 *
 * @param <T>
 */
public interface Source<T> extends Function<T,T>{
	/**
	 * Equivalent to calling {@link #apply(Object))} with 
	 * null.
	 * @return a value
	 */
	public T apply();
}
