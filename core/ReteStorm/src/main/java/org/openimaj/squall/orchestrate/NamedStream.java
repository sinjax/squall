package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link NamedStream} has a name, a start node, an end node and an ordered list of variables. 
 * The instances originating from the start which share the same value for the variables in
 * the list must all go to the same instance of the end node.  
 * @param <T> The {@link DGNode} this edge supports
 *
 */
public class NamedStream {
	
	
	protected static final String STREAM_KEY = "stream";
	String name;
	List<String> variables;
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 * @param start
	 * @param end
	 */
	public NamedStream(String name) {
		this.name = name;
		this.variables = new ArrayList<String>();
	}
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 * @param start
	 * @param end
	 * @param variables 
	 */
	public NamedStream(String name, List<String> variables) {
		this.name = name;
		this.variables = variables;
	}

	public String getName() {
		return this.name;
	}

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
