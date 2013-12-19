package org.openimaj.squall.compile.data.revised;

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
