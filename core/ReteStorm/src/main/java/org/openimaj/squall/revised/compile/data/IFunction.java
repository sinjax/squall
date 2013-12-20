package org.openimaj.squall.revised.compile.data;

import org.openimaj.util.function.MultiFunction;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A function which can be {@link #setup()} and {@link #cleanup()}
 *
 * @param <I>
 * @param <O>
 */
public interface IFunction<I,O> extends MultiFunction<I,O>, Initialisable{
	
}
