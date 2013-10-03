package org.openimaj.squall.data;

import java.util.List;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A given node in a compiled graph has information useful for building the graph
 * including:
 * 	the node's name
 * 	an anonymised name for the node (i.e. generic variables)
 * 	a list of variables
 *
 */
public interface ComponentInformation {
//	public String name(); // ?1 <cat> <dog>
//	public String anonymisedName(); // VAR <cat> <dog>
//	public List<String> variables(); // [ ?1 ]
	// [ ?cat ]
	// [ ?0 ]
}
