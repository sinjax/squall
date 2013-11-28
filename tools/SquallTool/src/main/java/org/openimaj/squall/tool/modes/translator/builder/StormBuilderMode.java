package org.openimaj.squall.tool.modes.translator.builder;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.rdf.storm.sparql.topology.builder.SPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.tool.SquallToolOptions;

import backtype.storm.Config;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormBuilderMode extends BuilderMode {
	
	/**
	 * The topology
	 */
	@Option(
			name = "--storm-topology-mode",
			aliases = "-stm",
			required = false,
			usage = "The kind of topology to submit to",
			handler = ProxyOptionHandler.class)
	public StormBuilderTopologyModeOption tm = StormBuilderTopologyModeOption.LOCAL;
	public StormBuilderTopologyMode tmOp = tm.getOptions();
	
	/**
	 * number of topology workers selected
	 */
	@Option(
			name = "--topology-workers",
			aliases = "-twork",
			required = false,
			usage = "The number of workers running the executors of this topology")
	public int numberOfWorkers = 2;
	
	@Option(
			name = "--topology-max-parallelism",
			aliases = "-maxpar",
			required = false,
			usage = "Max parallelism")
	private int maxParallelism = 4;

	@Override
	public void run(OrchestratedProductionSystem ops) {
		StormStreamBuilder ssb;
		try {
			ssb = tmOp.topologyOperation(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ssb.build(ops);
	}
	@Override
	public void setup(SquallToolOptions opts) {
	}
	
	/**
	 * @return
	 */
	public Config prepareConfig() {
		Config preparedConfig = new Config();
//		preparedConfig.setMaxSpoutPending(100);
//		preparedConfig.setNumWorkers(numberOfWorkers);
//		preparedConfig.setMaxTaskParallelism(maxParallelism);
		JenaStormUtils.registerSerializers(preparedConfig);
		
		return preparedConfig;
	}

}
