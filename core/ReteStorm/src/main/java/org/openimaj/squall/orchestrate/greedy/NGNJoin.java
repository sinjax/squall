package org.openimaj.squall.orchestrate.greedy;


import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.NNIVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NGNJoin extends NNIVFunction {

	
	/**
	 * @param parent
	 * @param name 
	 * @param left
	 * @param right
	 */
	public NGNJoin(OrchestratedProductionSystem parent, String name, NamedNode<? extends IVFunction<Context, Context>> left,NamedNode<? extends IVFunction<Context, Context>> right) {
		super(parent, name, new FixedJoinFunction(left.getData(), right.getData()));
		left.connect(this.leftNamedStream(), this);
		right.connect(this.rightNamedStream(), this);
	}

	/**
	 * @return named stream representing the link between the left and this join
	 */
	public NamedStream leftNamedStream() {
		return new NamedStream("left",((FixedJoinFunction)this.getData()).sharedVars());
	}
	
	/**
	 * @return named stream representing the link between the right and this join
	 */
	public NamedStream rightNamedStream() {
		return new NamedStream("right",((FixedJoinFunction)this.getData()).sharedVars());
	}

}
