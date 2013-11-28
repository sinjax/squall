package org.openimaj.squall.tool;

/**
 * Can be setup using the SquallToolOptions
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface SquallToolSetup {
	/**
	 * @param opts
	 */
	public void setup(SquallToolOptions opts);
	
	/**
	 * @param opts
	 */
	public void shutdown(SquallToolOptions opts);
}
