package org.openimaj.squall.compile;

import org.openimaj.util.function.Function;

/**
 * Apply a function to some input, producing an appropriate result.
 * Also provide an anonymous name of this function (i.e. a name where variables are anonymised)
 * 
 * @param <IN>
 *            the type of the input to the function.
 * @param <OUT>
 *            the type of the result.
 */
public interface AnonymousNameFunction<IN, OUT> extends Function<IN, OUT>{
	/**
	 * @return provide the anonymous name of this function
	 */
	public String anonymousName();
}
