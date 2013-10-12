package org.openimaj.squall.build.storm;

import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;

import backtype.storm.tuple.Tuple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OperationBolt extends ProcessingBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2034257684933988838L;
	private Operation<Context> op;
	/**
	 * @param nn
	 * @throws Exception
	 */
	public OperationBolt(NamedNode<?> nn) throws Exception {
		super(nn);
		if(nn.isOperation()) op = nn.getOperation();
		else{
			throw new Exception("Inappropriate node");
		}
	}

	@Override
	public void execute(Tuple input) {
		Context c = getContext(input);
		op.perform(c);
	}

}
