package org.openimaj.squall.build.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
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
	private Set<String> streams;
	private List<String> outputStreams;
	private HashMap<String, String> correctedStreamName;
	/**
	 * @param nn
	 */
	public NamedNodeComponent(NamedNode<?> nn) {
		
		new HashMap<String,Function<Context, Context>>();
		this.outputStreams = new ArrayList<String>();
		for (NamedStream edge : nn.childEdges()) {
				this.outputStreams.add(StormUtils.legalizeStormIdentifier(edge.identifier()));
		}
		this.correctedStreamName = new HashMap<String,String>();
		for (NamedStream edge : nn.parentEdges()) {
			String stormName = StormUtils.legalizeStormIdentifier(edge.identifier());
			this.correctedStreamName.put(stormName, edge.identifier());
		}
	}
	
	/**
	 * @param conf
	 * @param context
	 */
	public void setup(@SuppressWarnings("rawtypes") Map conf, TopologyContext context) {
		this.streams = context.getThisStreams();
	}

	/**
	 * 
	 */
	public void cleanup() {
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		for (String strm : this.outputStreams) {			
			declarer.declareStream(strm,new Fields("context"));
		}
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
		ctx.put(ContextKey.STREAM_KEY.toString(), this.correctedStreamName.get(t.getSourceStreamId()));
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
		for ( String  strm : this.streams) {
			Values em = new Values(ctx.clone());
			fire(strm, t, em);
		}
	}
	
	/**
	 * @param anchor 
	 * @param ret
	 */
	public void fire(Tuple anchor, List<Context> ret) {
		if(ret != null){			
			for (Context context : ret) {
				fire(anchor,context);
			}
		}
		
	}

	/**
	 * @param strm the streamId on which to emit
	 * @param anchor the anchor which cause this emit, might be null
	 * @param ctx this value to emit
	 */
	public abstract void fire(String strm, Tuple anchor, Values ctx) ;

	/**
	 * Given a parent, a stream and a namedNode construct the stream name in the right way
	 * @param parent
	 * @param strm
	 * @param namedNode
	 * @return stream named
	 */
	public static String constructStreamName(NamedNode<?> parent,NamedStream strm, NamedNode<?> namedNode) {
		
		return StormUtils.legalizeStormIdentifier(parent.getName() + "_" + strm.identifier() + "_" + namedNode.getName());
	}

}
