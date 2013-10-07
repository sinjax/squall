package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link NamedStream} has a name, a start node, an end node and an ordered list of variables. 
 * The instances originating from the start which share the same value for the variables in
 * the list must all go to the same instance of the end node.  
 * @param <T> The {@link DGNode} this edge supports
 *
 */
public class NamedStream<T extends DGNode<?, NamedStream<T>,?>> {
	
	
	String name;
	List<String> variables;
	T start;
	T end;
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 * @param start
	 * @param end
	 */
	public NamedStream(String name, T start, T end) {
		this.name = name;
		this.variables = new ArrayList<String>();
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Simple link, named, with a start and end
	 * @param name
	 * @param start
	 * @param end
	 * @param variables 
	 */
	public NamedStream(String name, T start, T end, List<String> variables) {
		this.name = name;
		this.variables = variables;
		this.start = start;
		this.end = end;
	}
	
	
}
