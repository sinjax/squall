package org.openimaj.squall.orchestrate;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author davidlmonks
 *
 */
public class ContextAugmentingFunction implements Function<Context, Context> {

	private static final String NAME_KEY = "information";
	private String sourceName;
	
	/**
	 * @param name
	 */
	public ContextAugmentingFunction(String name) {
		this.sourceName = name;
	}

	/**
	 * Adds various information to the {@link Context} received, modifying it, and returning it.
	 * @param in
	 * 		the {@link Context} to be augmented
	 * @return
	 * 		the augmented {@link Context}
	 */
	@Override
	public Context apply(Context in) {
		if(in == null) return null;
		in.put(NAME_KEY, this.sourceName);
		return in;
	}

}
