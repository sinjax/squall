package org.openimaj.squall.compile.data;

import org.openimaj.util.function.Operation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * An operation which is initialisable
 *
 * @param <I>
 */
public interface IOperation<I> extends Operation<I>, Initialisable{
	
}
