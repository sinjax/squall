package org.openimaj.squall.orchestrate.rete;


import java.util.List;

import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.NNIVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class NGNJoin extends NNIVFunction {
	
	/**
	 * @param parent
	 * @param name 
	 * @param leftNode
	 * @param rightNode
	 * @param wi 
	 */
	public NGNJoin(OrchestratedProductionSystem parent,
				   String name,
				   NamedNode<? extends IVFunction<Context, Context>> leftNode,
				   NamedNode<? extends IVFunction<Context, Context>> rightNode,
				   WindowInformation wi) {
		super(parent, name, new StreamAwareFixedJoinFunction(leftNode.getData(), wi, rightNode.getData(), wi));
		
		List<String> lsv = ((StreamAwareFixedJoinFunction) this.getData()).leftSharedVars();
		String[] leftSharedVars = lsv.toArray(new String[lsv.size()]);
		
		List<String> rsv = ((StreamAwareFixedJoinFunction) this.getData()).rightSharedVars();
		String[] rightSharedVars = rsv.toArray(new String[rsv.size()]);
		
		NamedStream leftStream = new NamedStream(leftNode.getVariableHolder().identifier(), leftSharedVars);
		leftNode.connectOutgoingEdge(leftStream);
		this.connectIncomingEdge(leftStream);
		
		NamedStream rightStream = new NamedStream(rightNode.getVariableHolder().identifier(), rightSharedVars);
		rightNode.connectOutgoingEdge(rightStream);
		this.connectIncomingEdge(rightStream);
	}

}
