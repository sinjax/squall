package org.openimaj.squall.build.storm;

import java.util.Map;

import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
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
	private IOperation<Context> op;
	private byte[] serializedOp;
	/**
	 * @param nn
	 * @throws Exception
	 */
	public OperationBolt(NamedNode<?> nn) throws Exception {
		super(nn);
		if(nn.isOperation()) {
			this.serializedOp = StormUtils.serialiseFunction(kryo,nn.getOperation());
		}
		else{
			throw new Exception("Inappropriate node");
		}
	}
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		this.op = StormUtils.deserialiseFunction(kryo,this.serializedOp);
		this.op.setup();
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		this.op.cleanup();
	}

	@Override
	public void execute(Tuple input) {
		Context c = getContext(input);
		op.perform(c);
	}

}
