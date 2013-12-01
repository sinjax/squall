package org.openimaj.squall.build.storm.topology;

import org.openimaj.squall.compile.data.IOperation;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormClusterOperation implements IOperation<StormTopology> {

	/**
	 * key used to extract topology names from configs
	 */
	public static final String TOPOLOGY_NAME_KEY = "org.openimaj.squall.topology.name";
	private Config conf;

	/**
	 * @param conf
	 */
	public StormClusterOperation(Config conf) {
		this.conf = conf;
	}

	@Override
	public void perform(StormTopology object) {
		try {
			StormSubmitter.submitTopology((String) conf.get(TOPOLOGY_NAME_KEY), conf, object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setup() {
	}

	@Override
	public void cleanup() {
	}

}
