package org.openimaj.rdf.storm.eddying;

import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author David Monks <am11g08@ecs.soton.ac.uk>
 */
public interface EddyingBolt{

	public String getName();
	/**
	 * Emit the tuple back to the last step of processing.
	 * @param anchor
	 * @param vals
	 */
	public void emit(Tuple anchor, Values vals);
	public void emit(String name, Tuple anchor, Values vals);
	public void ack(Tuple anchor);
	
}
