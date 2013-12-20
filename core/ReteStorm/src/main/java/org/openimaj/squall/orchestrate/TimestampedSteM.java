package org.openimaj.squall.orchestrate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <T> 
 *
 */
public interface TimestampedSteM <T> extends SteM <T> {

	/**
	 * @param typed
	 * @param timestamp 
	 * @param delay 
	 * @param unit 
	 * @return
	 * 		True if the object 'typed' was successfully built into the SteM. False otherwise.
	 */
	public boolean build(T typed, long timestamp, long delay, TimeUnit unit);
	
	/**
	 * @param typed
	 * @param timestamp 
	 * @param delay 
	 * @return
	 * 		True if the object 'typed' was successfully built into the SteM. False otherwise.
	 */
	public boolean build(T typed, long timestamp, long delay);
	
	/**
	 * @param typed
	 * @param timestamp 
	 * @return
	 * 		True if the object 'typed' was successfully built into the SteM. False otherwise.
	 */
	public boolean build(T typed, long timestamp);
	
}
