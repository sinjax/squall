package org.openimaj.squall.orchestrate.revised.rete;


import org.openimaj.squall.compile.data.revised.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.revised.IVFunction;
import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.orchestrate.revised.RevisedNNIVFunction;
import org.openimaj.squall.orchestrate.revised.RevisedNamedNode;
import org.openimaj.squall.orchestrate.revised.RevisedNamedStream;
import org.openimaj.squall.orchestrate.revised.RevisedOrchestratedProductionSystem;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class RevisedNGNJoin extends RevisedNNIVFunction {

	private RevisedNamedStream leftStream;
	private RevisedNamedStream rightStream;
	
	
	/**
	 * @param parent
	 * @param name 
	 * @param left
	 * @param right
	 * @param wi 
	 */
	public RevisedNGNJoin(RevisedOrchestratedProductionSystem parent, String name, RevisedNamedNode<? extends IVFunction<Context, Context>> left,RevisedNamedNode<? extends IVFunction<Context, Context>> right, WindowInformation wi) {
		super(parent, name, new StreamAwareFixedJoinFunction(left.getData(), left.getVariableHolder().anonimised(), wi, right.getData(), right.getVariableHolder().anonimised(), wi));
		left.connect(this.leftNamedStream(left.getVariableHolder()), this);
		right.connect(this.rightNamedStream(right.getVariableHolder()), this);
	}

	/**
	 * @param vh 
	 * @return named stream representing the link between the left and this join
	 */
	public RevisedNamedStream leftNamedStream(AnonimisedRuleVariableHolder vh) {
		return new RevisedNamedStream(vh.anonimised(),((StreamAwareFixedJoinFunction) this.getData()).leftSharedVars());
	}
	
	/**
	 * @param vh 
	 * @return named stream representing the link between the right and this join
	 */
	public RevisedNamedStream rightNamedStream(AnonimisedRuleVariableHolder vh) {
		return new RevisedNamedStream(vh.anonimised(),((StreamAwareFixedJoinFunction)this.getData()).rightSharedVars());
	}

}
