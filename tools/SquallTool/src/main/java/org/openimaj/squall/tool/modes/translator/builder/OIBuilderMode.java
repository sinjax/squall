package org.openimaj.squall.tool.modes.translator.builder;

import org.openimaj.squall.build.OIStreamBuilder;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.tool.SquallToolOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OIBuilderMode implements BuilderMode {

	@Override
	public void run(OrchestratedProductionSystem ops) {
		OIStreamBuilder oisb = new OIStreamBuilder();
		oisb.build(ops);
	}

	@Override
	public void setup(SquallToolOptions opts) {		
	}

}
