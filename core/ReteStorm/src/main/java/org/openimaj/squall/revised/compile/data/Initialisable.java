package org.openimaj.squall.revised.compile.data;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface Initialisable {
	/**
	 * 
	 */
	public void setup();
	
	/**
	 * 
	 */
	public void cleanup();
}
