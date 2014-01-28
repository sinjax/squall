package org.openimaj.squall.compile.data;

public interface Parallelisable {

	/**
	 * @return
	 * 		true if the object gathers no persistent state.
	 * 		i.e. any one parallel instance can be guaranteed to behave correctly and completely
	 */
	public boolean isStateless();
	
	/**
	 * @return
	 * 		true if the object cannot be parallelised
	 */
	public boolean forcedUnique();
	
}
