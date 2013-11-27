package org.openimaj.squall.tool.modes.translator.builder;

import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.tool.SquallToolOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormBuilderMode implements BuilderMode {

	@Override
	public void run(OrchestratedProductionSystem ops) {
		StormStreamBuilder ssb = createSSB();
		ssb.build(ops);
	}

	private StormStreamBuilder createSSB() {
		return null;
	}

	@Override
	public void setup(SquallToolOptions opts) {
	}

}
