package org.openimaj.squall.tool.modes.translator.builder;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.build.storm.topology.StormClusterOperation;
import org.openimaj.squall.build.storm.topology.StormClusterOperationTOF;

import backtype.storm.Config;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormTopologyMode implements StormBuilderTopologyMode {
	private static final Logger logger = Logger.getLogger(StormTopologyMode.class);
	
	/**
	 * number of topology workers selected
	 */
	@Option(
			name = "--topology-name",
			aliases = "-tname",
			required = true,
			usage = "Name of the topology to submit")
	public String name = null;
	
	
	

	
	@Override
	public StormStreamBuilder topologyOperation(StormBuilderMode sbm) throws Exception {
		Config conf = sbm.prepareConfig();
		conf.put(StormClusterOperation.TOPOLOGY_NAME_KEY, name);
		logger.info("\nStarting topology: \n" + conf);
		return new StormStreamBuilder(StormClusterOperationTOF.instance(), conf);
	}

}
