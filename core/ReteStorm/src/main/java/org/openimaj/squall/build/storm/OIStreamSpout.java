package org.openimaj.squall.build.storm;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OIStreamSpout extends NamedNodeComponent implements IRichSpout{

	/**
	 * 
	 */
	private static final long serialVersionUID = 792877157792845249L;
	private Stream<Context> stream;
	private ISource<Stream<Context>> streamSource;
	private SpoutOutputCollector collector;
	private byte[] serializedStreamSource;
	private static final Logger logger = Logger.getLogger(OIStreamSpout.class);

	/**
	 * @param namedNode
	 * @throws Exception 
	 */
	public OIStreamSpout(NamedNode<?> namedNode) throws Exception {
		super(namedNode);
		if(namedNode.isSource()) {
			this.serializedStreamSource = StormUtils.serialiseFunction(JenaStormUtils.kryo(),namedNode.getSource());
		}
		else{
			throw new Exception("Inappropriate node");
		}
	}


	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collector) {
		setup(conf,context);
		this.collector = collector;
		this.streamSource = StormUtils.deserialiseFunction(JenaStormUtils.kryo(),serializedStreamSource);
		logger.debug("Initialising stream: " + this.streamSource);
		this.streamSource.setup();
		this.stream = this.streamSource.apply();
	}

	@Override
	public void close() {
		cleanup();
	}

	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}
	int seen = 0;
	@Override
	public void nextTuple() {
		Context item = null;
		if(this.stream.hasNext()) 
		{
			item = this.stream.next();
		}
		if (item != null) {
			seen++;
			this.fire(item);
			if(seen%5000 == 0){
				logger.debug("Emitted " + seen);
//				Utils.sleep(100);
			}
		} else {
			logger.debug("Stream empty, waiting 10");
			Utils.sleep(10);
		}
		
	}

	@Override
	public void ack(Object msgId) { }

	@Override
	public void fail(Object msgId) { }


	@Override
	public void fire(String strm, Tuple anchor, Values ctx) {
		this.collector.emit(strm, ctx);
	}

}
