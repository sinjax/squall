package org.openimaj.squall.compile.data;

import org.openimaj.squall.data.ComponentInformation;
import org.openimaj.util.function.Function;

/**
 * Apply a function to some input, producing an appropriate result.
 * Also provides a {@link ComponentInformation} instance, useful for building 
 * 
 * @param <IN>
 *            the type of the input to the function.
 * @param <OUT>
 *            the type of the result.
 */
public interface ComponentInformationFunction<IN, OUT> extends Function<IN, OUT>{
	/**
	 * @return provide infromation useful for building
	 */
	public ComponentInformation information();
}
