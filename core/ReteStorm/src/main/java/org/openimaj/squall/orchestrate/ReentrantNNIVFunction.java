package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
		private static final Logger logger = Logger.getLogger(RIVFunction.class);
		@Override
		public List<Context> apply(Context in) {
			if(in.containsKey("bindings")){
				return new ArrayList<Context>();
			}
			logger.debug("Reentering: " + in);
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