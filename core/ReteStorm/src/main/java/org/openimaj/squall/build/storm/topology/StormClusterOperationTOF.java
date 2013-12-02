package org.openimaj.squall.build.storm.topology;

import org.openimaj.squall.compile.data.IOperation;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormClusterOperationTOF extends TopologyOperationFactory{

	private static StormClusterOperationTOF instance = null;

	@Override
	public IOperation<StormTopology> topop(Config conf) {
		return new StormClusterOperation(conf);
	}

	/**
	 * @return the instance of this factory
	 */
	public static StormClusterOperationTOF instance() {
		if(instance == null){
			instance  = new StormClusterOperationTOF();
		}
		return instance;
	}
	
}