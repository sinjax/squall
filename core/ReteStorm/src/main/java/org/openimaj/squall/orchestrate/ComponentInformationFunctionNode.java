package org.openimaj.squall.orchestrate;

import java.util.List;

import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.data.ComponentInformation;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link ComponentInformationFunctionNode} has a {@link ComponentInformation} instances and a {@link Function} which takes as input a 
 * {@link Context} and returns a {@link Context}. The {@link ComponentInformationFunctionNode} is a
 * function itself which wraps the internal {@link Function} call while embedding its own
 * name in the {@link Context} instance under the key NAME_KEY
 */
public class ComponentInformationFunctionNode extends DAGNode<ComponentInformationFunctionNode>{
	/**
	 * key used to insert this node's name into the returned context
	 */
	public static final String INFORMATION_KEY = "name";
	private ComponentInformation information;
	private Function<Context, Context> func;
	
	
	/**
	 * @param cif
	 */
	public ComponentInformationFunctionNode(ComponentInformationFunction<Context,Context> cif) {
		this.information = cif.information();
		this.func = cif;
	}
	/**
	 * @param information
	 * @param func
	 */
	public ComponentInformationFunctionNode(ComponentInformation information, Function<Context, Context> func) {
		this.information = information;
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
				out.put(INFORMATION_KEY, information);
				return out;
			}
		};
	}
	
	/**
	 * @return the component information of this node
	 */
	public ComponentInformation information(){
		return this.information;
	}
	

}
