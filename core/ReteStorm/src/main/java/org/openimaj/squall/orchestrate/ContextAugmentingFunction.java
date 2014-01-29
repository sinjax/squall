package org.openimaj.squall.orchestrate;

import java.io.Serializable;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author davidlmonks
 *
 */
public class ContextAugmentingFunction implements Function<Context, Context>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5178137783782497990L;
	
	private String key;
	private String value;
	
	/**
	 * @param key 
	 * @param value
	 */
	public ContextAugmentingFunction(String key, String value) {
		this.key = key;
		this.value = value;
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
		if(!in.containsKey(this.key)){
			in.put(this.key, this.value);
		}
		return in;
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private ContextAugmentingFunction(){}

}
