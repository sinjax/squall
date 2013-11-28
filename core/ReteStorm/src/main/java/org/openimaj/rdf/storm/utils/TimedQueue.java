package org.openimaj.rdf.storm.utils;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public interface TimedQueue<T> extends TimeLimitedCollection, Queue<T> {
	
	/**
	 * Adds a new object of type T to the time-based priority queue.  A timestamp should be generated for the object by the Collection.
	 * @param arg0
	 * 		The datum
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0);
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object.
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long timestamp);
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue (in milliseconds).
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span, defined in the unit of the Window
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long timestamp, long delay);
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue, given in the specified time unit.
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span
	 * @param unit
	 * 		the time unit that the datum-specific life span is defined in
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long timestamp, long delay, TimeUnit unit);
	
	/**
	 * Offers a new object of type T to the time-based priority queue.  A timestamp should be generated for the object by the Collection.
	 * @param arg0
	 * 		The datum
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0);
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object.
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long timestamp);
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue (in milliseconds).
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span, defined in the unit of the Window
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long timestamp, long delay);
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue, given in the specified time unit.
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span
	 * @param unit
	 * 		the time unit that the datum-specific life span is defined in
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long timestamp, long delay, TimeUnit unit);
	
}
