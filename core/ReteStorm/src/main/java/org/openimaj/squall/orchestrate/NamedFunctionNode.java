package org.openimaj.squall.orchestrate;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link NamedFunctionNode} has a name and a {@link Function} which takes as input a 
 * {@link Context} and returns a {@link Context}. The {@link NamedFunctionNode} is a
 * function itself which wraps the internal {@link Function} call while embedding its own
 * name in the {@link Context} instance under the key NAME_KEY
 */
public class NamedFunctionNode extends DAGNode<NamedFunctionNode>{
	/**
	 * key used to insert this node's name into the returned context
	 */
	public static final String NAME_KEY = "name";
	private String name;
	private Function<Context, Context> func;
	
	/**
	 * @param name
	 * @param func
	 */
	public NamedFunctionNode(String name, Function<Context, Context> func) {
		this.name = name;
		this.func = func;
	}
	
	
	/**
	 * @param in
	 * @return the function wrapping this node's behavior
	 */
	public Function<Context,Context> apply(Context in) {
		return new Function<Context, Context>() {
			
			@Override
			public Context apply(Context in) {
				Context out = func.apply(in);
				out.put(NAME_KEY, name);
				return out;
			}
		};
	}

}
