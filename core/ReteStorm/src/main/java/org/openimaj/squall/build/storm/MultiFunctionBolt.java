package org.openimaj.squall.build.storm;

import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.MultiFunction;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MultiFunctionBolt extends ProcessingBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2034257684933988838L;
	private MultiFunction<Context, Context> fun;
	private byte[] serializedFun;
	/**
	 * @param nn
	 * @throws Exception
	 */
	public MultiFunctionBolt(NamedNode<?> nn) throws Exception {
		super(nn);
		if(nn.isFunction()) {
			MultiFunction<Context, Context> function = nn.getFunction();
			this.serializedFun = StormUtils.serialiseFunction(JenaStormUtils.kryo(),function);
		}
		else{
			throw new Exception("Inappropriate node");
		}
	}
	
	protected byte[] getSerialisedFunction(){
		return this.serializedFun;
	}
	
	protected MultiFunction<Context, Context> getFunction(){
		return this.fun;
	}
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		this.fun = StormUtils.deserialiseFunction(JenaStormUtils.kryo(),this.serializedFun );
	}

	@Override
	public void execute(Tuple input) {
		Context c = getContext(input);
		List<Context> ret = fun.apply(c);
		this.fire(input,ret);
	}

}
