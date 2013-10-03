package org.openimaj.squall.orchestrate;

import java.util.List;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A {@link NamedStream} has a name, a start node, an end node and an ordered list of variables. 
 * The instances originating from the start which share the same value for the variables in
 * the list must all go to the same instance of the end node.  
 * @param <T> The {@link DGNode} this edge supports
 *
 */
public class NamedStream<T extends DGNode<T, NamedStream<T>>> {
	String name;
	List<String> variables;
	T start;
	T end;
}
