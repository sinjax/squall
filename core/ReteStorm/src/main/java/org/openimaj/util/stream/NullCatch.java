package org.openimaj.util.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.stream.Stream;

/**
 * A null catch forces a {@link Stream} which might return null to instead
 * block and wait for a not-null item.
 * 
 * This is achieved by making the {@link MultiFunction} return an empty {@link List} 
 * which most {@link Stream#map(MultiFunction)} implementations use as an opportunity 
 * to block and wait for items
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public final class NullCatch<T> implements MultiFunction<T,T> {
	@Override
	public List<T> apply(T in) {
		if(in == null) return new ArrayList<T>();
		return Arrays.asList(in);
	}
}