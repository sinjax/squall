package org.openimaj.squall.tool.modes.translator.builder;

import org.kohsuke.args4j.Option;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.build.storm.topology.LocalClusterOperation;
import org.openimaj.squall.build.storm.topology.LocalClusterOperationTOF;
import org.openimaj.squall.compile.data.IOperation;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LocalTopologyMode implements StormBuilderTopologyMode {

	
	private static final long DEFAULT_SLEEP_TIME = 5000;
	/**
	 * Time to wait in local mode
	 */
	@Option(
			name = "--sleep-time",
			aliases = "-st",
			required = false,
			usage = "How long the local topology should wait while processing happens, -1 waits forever",
			metaVar = "STRING")
	public long sleepTime = DEFAULT_SLEEP_TIME;
	
	@Override
	public StormStreamBuilder topologyOperation(StormBuilderMode sbm) throws Exception {
		Config conf = sbm.prepareConfig();
		conf.put(LocalClusterOperation.SLEEPKEY, sleepTime);
		return new StormStreamBuilder(LocalClusterOperationTOF.instance(), conf);
	}

}
