package org.openimaj.squall.orchestrate.greedy;

import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedFunctionNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NamedJoinNode extends NamedVarFunctionNode {

	

	static class JoinVariableFunction implements VariableFunction<Context, Context>{

		private VariableFunction<Context, Context> left;
		private VariableFunction<Context, Context> right;

		public JoinVariableFunction(
				VariableFunction<Context,Context> left,
				VariableFunction<Context,Context> right) {
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
		
	}

	private NamedVarFunctionNode left;
	private NamedVarFunctionNode right;
	
	/**
	 * @param name 
	 * @param left
	 * @param right
	 */
	public NamedJoinNode(String name, NamedVarFunctionNode left,NamedVarFunctionNode right) {
		super(name, new JoinVariableFunction(left.getVarFunc(),right.getVarFunc()));
		this.left = left;
		this.right = right;
	}

	/**
	 * @return named stream representing the link between the left and this join
	 */
	public NamedStream<NamedFunctionNode> leftNamedStream() {
		return new NamedStream<NamedFunctionNode>("left", left, this);
	}
	
	/**
	 * @return named stream representing the link between the right and this join
	 */
	public NamedStream<NamedFunctionNode> rightNamedStream() {
		return new NamedStream<NamedFunctionNode>("right", right, this);
	}

}
