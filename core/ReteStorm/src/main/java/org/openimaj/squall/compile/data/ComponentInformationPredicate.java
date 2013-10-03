package org.openimaj.squall.compile.data;

import org.openimaj.squall.data.ComponentInformation;
import org.openimaj.util.function.Predicate;

/**
 * Apply a {@link Predicate} to some input, judge whether the input has passed.
 * Also provides a {@link ComponentInformation} instance, useful for building 
 * 
 * @param <IN>
 *            the type of the input to the predicate.
 */
public interface ComponentInformationPredicate<IN> extends Predicate<IN>{
	/**
	 * @return provide infromation useful for building
	 */
	public ComponentInformation information();
}
