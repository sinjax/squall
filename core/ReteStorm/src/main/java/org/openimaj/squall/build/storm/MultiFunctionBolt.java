package org.openimaj.squall.build.storm;

import java.util.List;

import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.MultiFunction;

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
	/**
	 * @param nn
	 * @throws Exception
	 */
	public MultiFunctionBolt(NamedNode<?> nn) throws Exception {
		super(nn);
		if(nn.isFunction()) fun = nn.getFunction();
		else{
			throw new Exception("Inappropriate node");
		}
	}

	@Override
	public void execute(Tuple input) {
		Context c = getContext(input);
		List<Context> ret = fun.apply(c);
		this.fire(input,ret);
	}

}
