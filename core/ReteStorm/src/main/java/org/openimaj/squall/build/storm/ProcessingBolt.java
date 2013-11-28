package org.openimaj.squall.build.storm;

import java.util.Map;

import org.openimaj.squall.orchestrate.NamedNode;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class ProcessingBolt extends NamedNodeComponent implements IRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2034257684933988838L;
	private OutputCollector collector;
	/**
	 * @param nn
	 * @throws Exception
	 */
	public ProcessingBolt(NamedNode<?> nn) throws Exception {
		super(nn);
	}

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		setup(stormConf, context);
		this.collector = collector;
	}

	@Override
	public void cleanup() {
		super.cleanup();
	}

	
	@Override
	public void fire(String strm, Tuple anchor, Values ctx) {
		this.collector.emit(strm, anchor, ctx);
	}

}
