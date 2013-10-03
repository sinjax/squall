package org.openimaj.squall.orchestrate.greedy;

import java.util.Map;

import org.openimaj.squall.compile.data.ComponentInformationPredicate;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <A>
 */
public class FilterFunction<A> implements Function<A, A> {

	private Predicate<A> pred;

	/**
	 * @param pred
	 */
	public FilterFunction(Predicate<A> pred) {
		this.pred = pred;
	}

	@Override
	public A apply(A in) {
		if(!pred.test(in))
			return null;
		return in;
	}

}
