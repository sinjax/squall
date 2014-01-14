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
	
	
	protected static final String STREAM_KEY = "stream";
	String name;
	
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
		StringBuilder n = new StringBuilder(name).append("[");
		if (streamVars.length > 0){
			int i = 0;
			n.append(streamVars[i]);
			this.addVariable(streamVars[i]);
			for (i++; i < streamVars.length - 1; i++){
				n.append(",").append(streamVars[i]);
				this.addVariable(streamVars[i]);
			}
		}
		n.append("]");
		this.name = n.toString();
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
	
	/**
	 * @return
	 * 		A reference to the stream of this name.
	 */
	public NamedStream duplicate(){
		return new NamedStream(this.name, this.variables());
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			NamedStream other = (NamedStream) obj;
			return this.name.equals(other.identifier());
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String identifier() {
		return this.name;
	}
}
