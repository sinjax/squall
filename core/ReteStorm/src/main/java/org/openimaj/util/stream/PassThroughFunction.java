package org.openimaj.util.stream;

import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PassThroughFunction<T> implements Function<T, T>{

	@Override
	public T apply(T in) {
		return in;
	}

}
