package org.openimaj.squall.orchestrate;

import java.util.List;
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
public class NamedStream extends VariableHolder{
	
	protected String name;
	protected String identifier;
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 */
	public NamedStream(String name) {
		super();
		this.name = name;
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
	public NamedStream(String name, String[] streamVars) {
		super();
		this.name = name;
		StringBuilder i = new StringBuilder(name).append("[");
		if (streamVars.length > 0){
			int v = 0;
			i.append("?").append(streamVars[v]);
			this.addVariable(streamVars[v]);
			for (v++; v < streamVars.length - 1; v++){
				i.append(",").append("?").append(streamVars[v]);
				this.addVariable(streamVars[v]);
			}
		}
		i.append("]");
		this.identifier = i.toString();
	}

	/**
	 * @return This function augments a {@link Context} with the name of this {@link NamedStream}
	 */
	public Function<Context,Context> getFunction(){
		return new ContextAugmentingFunction(ContextAugmentingFunction.STREAM_KEY, this.identifier);
	}
	
	/**
	 * @return
	 * 		A reference to the stream of this name.
	 */
	public NamedStream duplicate(){
		return new NamedStream(this.name.split("[")[0], this.variables());
	}
	
	@Override
	public String toString() {
		return this.identifier;
	}
	
	@Override
	public boolean equals(Object obj) {
		NamedStream other;
		try {
			other = (NamedStream) obj;
		} catch (ClassCastException e) {
			return false;
		}
		if (!this.name.equals(other.name)){
			return false;
		}
		if (this.varCount() != other.varCount()){
			return false;
		}
		for (int i = 0; i < this.varCount(); i++){
			if (!this.getVariable(i).equals(other.getVariable(i))){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}

	@Override
	public String identifier() {
		return this.identifier;
	}
}
