package org.openimaj.squall.compile.data;

import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A function which can be {@link #setup()} and {@link #cleanup()}
 *
 * @param <I>
 * @param <O>
 */
public interface IFunction<I,O> extends Function<I,O>, Initialisable{
	
}
