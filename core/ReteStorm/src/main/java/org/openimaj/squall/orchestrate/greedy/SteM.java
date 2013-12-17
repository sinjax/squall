package org.openimaj.squall.orchestrate.greedy;

import java.util.List;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public interface SteM<T> {

	/**
	 * @param typed
	 * @return
	 * 		True if the object 'typed' was successfully built into the SteM. False otherwise.
	 */
	public boolean build(T typed);

	/**
	 * @param typed
	 * @return
	 * 		The list of objects in the SteM that match the object 'typed'.
	 */
	public List<T> probe(T typed);
	
}
