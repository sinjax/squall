package org.openimaj.squall.orchestrate;

import java.util.List;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.greedy.PassThroughConsequence;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReentrantNNIVFunction extends NNIVFunction{

	private static class RIVFunction extends PassThroughConsequence{
		
		@Override
		public List<Context> apply(Context in) {
			return super.apply(in);
		}
	}

	/**
	 * @param parent
	 * @param name
	 */
	public ReentrantNNIVFunction(OrchestratedProductionSystem parent, String name) {
		super(parent, name, new RIVFunction());
	}

	@Override
	public boolean isSource() {
		return true;
	}
	@Override
	public boolean isReentrantSource() {
		return true;
	}
	
}