package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.data.ComponentInformation;
import org.openimaj.squall.orchestrate.NamedFunctionNode;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NamedJoinNode extends NamedFunctionNode {

	

	static class ComponentInformationJoinFunction implements ComponentInformationFunction<Context, Context>{

		public ComponentInformationJoinFunction(
				ComponentInformation left,
				ComponentInformation right) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public Context apply(Context in) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ComponentInformation information() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	/**
	 * @param left
	 * @param right
	 */
	public NamedJoinNode(String name, ComponentInformation left,ComponentInformation right) {
		super(name, new ComponentInformationJoinFunction(left,right));
	}
}
