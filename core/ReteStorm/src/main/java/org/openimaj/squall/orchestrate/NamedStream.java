package org.openimaj.squall.orchestrate;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link NamedStream} has a name, a start node, an end node and an ordered list of variables. 
 * The instances originating from the start which share the same value for the variables in
 * the list must all go to the same instance of the end node.
 *
 */
public class NamedStream {
	
	
	protected static final String STREAM_KEY = "stream";
	String name;
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 */
	public NamedStream(String name) {
		this.name = name;
	}
	
	/**
	 * @return its name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return This function augments a {@link Context} with the name of this {@link NamedStream}
	 */
	public Function<Context,Context> getFunction(){
		return new Function<Context, Context>() {
			
			@Override
			public Context apply(Context in) {
				in.put(STREAM_KEY, name);
				return in;
			}
		};
	}
}
