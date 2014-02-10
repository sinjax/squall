package org.openimaj.squall.compile.data;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface IConsequence extends Cloneable {
	
	/**
	 * @return
	 * 		true - if any consequences fired are to be recycled back into the network
	 * 		false - otherwise
	 */
	public boolean isReentrant();
	
}
