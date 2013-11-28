package org.openimaj.rdf.storm.utils;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public interface SpaceLimitedCollection {

	/**
	 * Prunes items from the queue if they have expired at the time of the call (items are expired if their timestamp + duration &lt; Now).
	 * @return
	 * 		The number of items of data pruned from the queue.
	 */
	public void pruneToCapacity();
	
}
