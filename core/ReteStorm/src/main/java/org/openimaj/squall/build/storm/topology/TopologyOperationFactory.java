package org.openimaj.squall.build.storm.topology;

import org.openimaj.squall.compile.data.IOperation;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class TopologyOperationFactory {
	/**
	 * @param conf
	 * @return an operation which handles a {@link StormTopology}
	 */
	public abstract IOperation<StormTopology> topop(Config conf);
}