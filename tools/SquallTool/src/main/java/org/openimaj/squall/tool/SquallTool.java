package org.openimaj.squall.tool;

import java.io.IOException;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SquallTool {
	private SquallToolOptions opts;

	/**
	 * @param opts
	 */
	public SquallTool(SquallToolOptions opts) {
		this.opts = opts;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		SquallTool st = new SquallTool(new SquallToolOptions(args));
		st.run();
	}

	private void run() {
		try{
			this.opts.setup();
			CompiledProductionSystem cps = opts.tmOp.cps();
			OrchestratedProductionSystem ops = opts.pmOp.ops(cps);
			opts.bmOp.run(ops);
			System.out.println("ALL DONE");
		} finally {
			this.opts.shutdown();
		}
	}
}
