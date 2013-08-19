package org.openimaj.rdf.storm.eddying;

import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author David Monks <am11g08@ecs.soton.ac.uk>
 */
public interface EddyingBolt{

	/**
	 * @return
	 * 		The name of the bolt
	 */
	public String getName();
	/**
	 * @param anchor
	 * 		The reference tuple to acknowledge
	 * @param vals
	 * 		The values in the tuple
	 */
	public void emit(Tuple anchor, Values vals);
	/**
	 * @param name
	 * 		Name of the stream to emit to
	 * @param anchor
	 * 		The reference tuple to acknowledge
	 * @param vals
	 * 		The values in the tuple
	 */
	public void emit(String name, Tuple anchor, Values vals);
	/**
	 * @param anchor
	 * 		The reference tuple to acknowledge
	 */
	public void ack(Tuple anchor);
	
}
