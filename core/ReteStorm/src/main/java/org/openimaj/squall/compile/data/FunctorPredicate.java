package org.openimaj.squall.compile.data;

import java.util.Map;

import org.openimaj.squall.data.ComponentInformation;

import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A predicate constructed from Jena {@link Functor} instances
 *
 */
public class FunctorPredicate implements ComponentInformationPredicate<Map<String, String>> {
	
	private Functor clause;

	/**
	 * @param clause
	 */
	public FunctorPredicate(Functor clause) {
		this.clause = clause;
	}

	@Override
	public boolean test(Map<String, String> object) {
		return false;
	}

	@Override
	public ComponentInformation information() {
		return null;
	}
}