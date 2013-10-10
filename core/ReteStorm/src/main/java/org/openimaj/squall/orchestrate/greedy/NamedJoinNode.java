package org.openimaj.squall.orchestrate.greedy;

import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NamedJoinNode extends NamedIVFunctionNode {

	

	static class JoinVariableFunction implements IVFunction<Context, Context>{

		private IVFunction<Context, Context> left;
		private IVFunction<Context, Context> right;

		public JoinVariableFunction(
				IVFunction<Context,Context> left,
				IVFunction<Context,Context> right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public Context apply(Context in) {
			if(in.getTyped("stream").equals("left")){
				
			}
			else if(in.getTyped("stream").equals("right")){
				
			}
			return null;
		}

		@Override
		public List<String> variables() {
			// TODO FIGURE OUT THE JOIN VARIABLES, if any
			return null;
		}

		@Override
		public String anonimised(Map<String, Integer> varmap) {
			return left.anonimised(varmap) + " " + right.anonimised(varmap);
		}

		@Override
		public String anonimised() {
			return left.anonimised() + " " + right.anonimised();
		}

		@Override
		public void setup() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub
			
		}
		
	}

	private NamedNode<? extends IVFunction<Context, Context>> left;
	private NamedNode<? extends IVFunction<Context, Context>> right;
	
	/**
	 * @param name 
	 * @param left
	 * @param right
	 */
	public NamedJoinNode(OrchestratedProductionSystem parent, String name, NamedNode<? extends IVFunction<Context, Context>> left,NamedNode<? extends IVFunction<Context, Context>> right) {
		super(parent, name, new JoinVariableFunction(left.getData(), right.getData()));
		this.left = left;
		this.right = right;
		left.connect(this.leftNamedStream(), this);
		right.connect(this.rightNamedStream(), this);
	}

	/**
	 * @return named stream representing the link between the left and this join
	 */
	public NamedStream leftNamedStream() {
		return new NamedStream("left");
	}
	
	/**
	 * @return named stream representing the link between the right and this join
	 */
	public NamedStream rightNamedStream() {
		return new NamedStream("right");
	}

}
