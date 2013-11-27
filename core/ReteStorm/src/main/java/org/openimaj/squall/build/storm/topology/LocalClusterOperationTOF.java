package org.openimaj.squall.build.storm.topology;

import org.openimaj.squall.compile.data.IOperation;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LocalClusterOperationTOF extends TopologyOperationFactory{

	private static LocalClusterOperationTOF instance = null;

	@Override
	public IOperation<StormTopology> topop(Config conf) {
		return new LocalClusterOperation(conf);
	}

	/**
	 * @return the instance of this factory
	 */
	public static LocalClusterOperationTOF instance() {
		if(instance == null){
			instance  = new LocalClusterOperationTOF();
		}
		return instance;
	}
	
}