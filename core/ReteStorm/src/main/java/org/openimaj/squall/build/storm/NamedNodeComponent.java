package org.openimaj.squall.build.storm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IComponent;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class NamedNodeComponent implements IComponent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 843143889101579968L;
	private Initialisable init;
	private Set<String> streams;
	/**
	 * @param nn
	 */
	public NamedNodeComponent(NamedNode<?> nn) {
		if(nn.isInitialisable()) this.init = nn.getInit();
		new HashMap<String,Function<Context, Context>>();
	}
	
	/**
	 * @param conf
	 * @param context
	 */
	public void setup(@SuppressWarnings("rawtypes") Map conf, TopologyContext context) {
		if(init!=null) init.setup();
		this.streams = context.getThisStreams();
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		if(init!=null) init.cleanup();
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("context"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
	
	/**
	 * Register a stream and a function which must be applied before emitting to it
	 * @param t 
	 * @return the context from the tuple enriched with extra required information
	 */
	public Context getContext(Tuple t) {
		Context ctx = (Context) t.getValueByField("context");
		ctx.put("stream", t.getSourceStreamId());
		return ctx;
	}
	
	/**
	 * Fire this context to all listening streams
	 * @param ctx 
	 */
	public void fire(Context ctx) {
		fire(null,ctx);
	}
	
	/**
	 * Fire this context to all listening streams
	 * @param t 
	 * @param ctx 
	 */
	public void fire(Tuple t, Context ctx) {
		Values em = new Values(ctx);
		for ( String  strm : this.streams) {
			fire(strm, t, em);
		}
	}
	
	/**
	 * @param anchor 
	 * @param ret
	 */
	public void fire(Tuple anchor, List<Context> ret) {
		for (Context context : ret) {
			fire(anchor,context);
		}
	}

	/**
	 * @param strm the streamId on which to emit
	 * @param anchor the anchor which cause this emit, might be null
	 * @param ctx this value to emit
	 */
	public abstract void fire(String strm, Tuple anchor, Values ctx) ;

}
