package org.openimaj.squall.build.storm;

import java.util.Map;

import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.MultiFunction;

import scala.actors.threadpool.Arrays;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class IFunctionBolt extends MultiFunctionBolt {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3164888356541125857L;
	private Initialisable init;
	private byte[] serializedInit;
	
	/**
	 * @param nn
	 * @throws Exception
	 */
	public IFunctionBolt(NamedNode<?> nn) throws Exception {
		super(nn);
		if (nn.isInitialisable()){
			if (nn.getInit() != nn.getFunction()){
				this.serializedInit = StormUtils.serialiseFunction(JenaStormUtils.kryo(),nn.getInit());
			}
		}else{
			throw new Exception("Inappropriate Node");
		}
	}
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		if (this.serializedInit != null) {
			this.init = StormUtils.deserialiseFunction(JenaStormUtils.kryo(),this.serializedInit);
		} else {
			this.init = ((Initialisable) super.getFunction());
		}
		this.init.setup();
	}
	
	@Override
	public void cleanup() {
		this.init.cleanup();
		super.cleanup();
	}

}
