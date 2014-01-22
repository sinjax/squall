package org.openimaj.squall.compile.data;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface Initialisable extends KryoSerializable {
	/**
	 * 
	 */
	public void setup();
	
	/**
	 * 
	 */
	public void cleanup();
}
