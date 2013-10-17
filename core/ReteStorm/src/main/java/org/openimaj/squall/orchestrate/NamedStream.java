package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link NamedStream} has a name, a start node, an end node and an ordered list of variables. 
 * The instances originating from the start which share the same value for the variables in
 * the list must all go to the same instance of the end node.
 *
 */
public class NamedStream implements VariableHolder{
	
	
	protected static final String STREAM_KEY = "stream";
	String name;
	private List<String> streamVars;
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 */
	public NamedStream(String name) {
		this.name = name;
		this.streamVars = null;
	}
	
	/**
	 * This constructor allows the definition of stream variables as well as its name.
	 * The stream variables define which outputs from this source of this stream
	 * must be guaranteed to appear in the same instance of the destination.
	 * 
	 * In distributed streaming architectures this is useful as it results in
	 * all {@link Context} flowing on this stream which share certain variables
	 * to appear in the same physical instance
	 * 
	 * @param name the name of this stream
	 * @param streamVars the variables which are required up stream on this stream
	 */
	public NamedStream(String name, List<String> streamVars) {
		this.name = name;
		this.streamVars = streamVars;
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
				Context clone = in.clone();
				clone.put(STREAM_KEY, name);
				return clone;
			}
		};
	}

	@Override
	public List<String> variables() {
		return this.streamVars;
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String anonimised() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Implement Variable Mapping
		
	}
}
