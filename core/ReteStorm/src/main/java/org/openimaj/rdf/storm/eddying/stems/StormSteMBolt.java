/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.eddying.stems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.EddyingBolt;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.Action;
import org.openimaj.rdf.storm.eddying.stems.StormSteMQueue.Environment;
import org.openimaj.rdf.storm.topology.logging.LoggerBolt.LogEmitter;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.MessageId;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class StormSteMBolt implements IRichBolt, EddyingBolt {
	
	private static final long serialVersionUID = -6233820433299486911L;
	private static Logger logger = Logger.getLogger(StormSteMBolt.class);

	/**
	 * Meta-Data components of the storm values/fields list
	 *
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>, Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Component {

		/**
		 * 
		 */
		action
		,
		/**
		 *
		 */
		isAdd
		,
		/**
		 *
		 */
		graph
		,
		/**
		 *
		 */
		timestamp
		,
		/**
		 *
		 */
		duration
		;
		private static String[] strings;
		static {
			Component[] vals = Component.values();
			strings = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strings[i] = vals[i].toString();
			}
		}

		/**
		 * @return like {@link #values()} but {@link String} instances
		 */
		public static String[] strings() {
			return strings;
		}
	}
	
	protected String name;
	protected String patternString;
	protected StormGraphRouter router;
	
	protected int vars = 3;
	protected int size = 5000;
	protected long delay = 600000;
	protected TimeUnit unit = TimeUnit.MILLISECONDS;
	
	/**
	 * Create a new SteM with fully customised characteristics.
	 * @param vars - variables available for matching
	 * @param size - capacity of the window
	 * @param delay - maximum age of graphs in the window
	 * @param unit - unit of time used
	 * @param name - the name of the SteM in the Storm topology.
	 * @param pattern - the pattern of triples stored in the bolt's window.
	 * @param sgr - the graph routing object for use by the SteM.
	 */
	public StormSteMBolt(String name,
						 String pattern,
						 StormGraphRouter sgr,
						 int vars,
						 int size,
						 long delay,
						 TimeUnit unit) {
		this(name,pattern,sgr);
		this.vars = vars;
		this.size = size;
		this.delay = delay;
		this.unit = unit;
	}
	
	/**
	 * Create a new SteM with default queue characteristics:
	 * <ul>
	 * 	<li>variables available for matching: 3</li>
	 * 	<li>capacity of the window: 5000 graphs</li>
	 * 	<li>maximum age of graphs in the window: 10 units</li>
	 * 	<li>unit of time used: minutes</li>
	 * </ul>
	 * @param name - the name of the SteM in the Storm topology.
	 * @param pattern - the pattern of triples stored in this bolt's window.
	 * @param sgr - the graph routing object for use by the SteM.
	 */
	public StormSteMBolt(String name, String pattern, StormGraphRouter sgr) {
		this.name = name;
		this.patternString = pattern;
		this.router = sgr;
	}

	private Map<String,Object> conf;
	@SuppressWarnings("unused")
	private TopologyContext context;
	private OutputCollector collector;
	
	protected TripleMatch pattern;
	
	protected Map<MessageId,Integer> buildCount;
	protected StormSteMQueue window;
	
	@SuppressWarnings("unchecked")
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf,
						TopologyContext context,
						OutputCollector collector) {
		this.conf = stormConf;
		this.context = context;
		this.collector = collector;
		this.router.setOutputCollector(this);
		
		this.pattern = ((TriplePattern)Rule.parseRule(patternString).getBody()[0]).asTripleMatch();
		
		this.buildCount = new HashMap<MessageId,Integer>();
		this.window = new StormSteMQueue(vars, size, delay, unit, new LogEmitter(collector), router);
	}
	
	@Override
	public void cleanup() {
		this.conf = null;
		this.context = null;
		this.collector = null;
		this.router.cleanup();
		
		this.pattern = null;
		
		this.buildCount = null;
		this.window = null;
	}

	@Override
	public void execute(Tuple input) {
logger.debug(String.format("Performing %s with %s at SteM %s.",((Action)input.getValueByField(Component.action.toString())).toString(),
															   ((Graph)input.getValueByField(Component.graph.toString())).toString(),
															   this.name));
		switch (input.getSourceStreamId().charAt(0)) {
			// Case for 'c'ontrol tuples
			case 'c':
			// Case for all other tuples (data tuples)
			default:
				List<Node> ges = new ArrayList<Node>();
				for (int i = 0; i < vars; i++)
					try {
						ges.add((Node)input.getValue(i));
					} catch (ClassCastException e) {
						if (input.getValueByField(Component.action.toString()).equals(Action.check))
							break;
						else{
							e.printStackTrace();
							System.exit(1);
						}
					}
				execute(input,
					/*Nodes*/	ges,
					(Action)	input.getValueByField(Component.action.toString()),
					/*isAdd*/	input.getBooleanByField(Component.isAdd.toString()),
					(Graph)		input.getValueByField(Component.graph.toString()),
					/*timestmp*/input.getLongByField(Component.timestamp.toString()),
					/*duration*/input.getLongByField(Component.duration.toString()));
		}
	}
	
	private void execute(Tuple input,
						 List<Node> graphElements,
						 Action action, boolean isAdd, Graph g, Long timestamp, Long duration){
		switch (action){
			case check:
				logger.debug(String.format("\nSteM %s checking validity of triple: %s", this.name,
						   g.toString()));
				Triple newTriple = g.find(null,null,null).next();
				Node subject = newTriple.getSubject();
				Node predicate = newTriple.getPredicate();
				Node object = newTriple.getObject();
				if ((pattern.getMatchSubject() == null || pattern.getMatchSubject().sameValueAs(subject))
						&& (pattern.getMatchPredicate() == null || pattern.getMatchPredicate().sameValueAs(predicate))
						&& (pattern.getMatchObject() == null || pattern.getMatchObject().sameValueAs(object))){
					logger.debug(String.format("\nSteM %s routing for further checking/building the triple: %s", this.name,
							   g.toString()));
					graphElements.add(subject);
					graphElements.add(predicate);
					graphElements.add(object);
					//TODO Change to tree-select and build once proper hierarchical SteMs are produced.
// 					do not do the following in implicit eddy systems, and do not break unless triple does not match
//					this.router.routeTriple(input, Action.build, isAdd, g, timestamp);
				} else
					break;	
			case build:
				logger.debug(String.format("\nSteM %s building in environment: %s", this.name,
						   graphElements.toString()));
				this.window.build(isAdd, graphElements, g, timestamp, duration);
				this.router.routeGraph(input, Action.probe, isAdd,
						   (Graph) input.getValueByField(StormSteMBolt.Component.graph.toString()),
						   timestamp);
				break;
			case probe:
				logger.debug(String.format("\nSteM %s being probed with environment: %s", this.name,
						   graphElements.toString()));
				this.window.probe(input, isAdd, timestamp, duration);
				break;
			default:
				System.out.println("PANIC!!!!!");
				System.out.println(input.getValues());
				System.exit(1);
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void emit(Tuple anchor, Values vals) {
		this.collector.emit(anchor,vals);
	}
	
	@Override
	public void emit(String name, Tuple anchor, Values vals) {
		if (name.equals(this.name)){
			List<Node> ges = new ArrayList<Node>();
			for (int i = 0; i < vars; i++)
				ges.add((Node)vals.get(i));
			execute(anchor,
					/*Nodes*/ges,
					(Action)vals.get(3),
					(Boolean)vals.get(4),
					(Graph)vals.get(5),
					(Long)vals.get(6),
					(Long)vals.get(7));
		} else{
			logger.debug(String.format("\nRouting triple: %s %s %s\nto SteM: %s",
					   vals.get(0),
					   vals.get(1),
					   vals.get(2),
					   name));
			this.collector.emit(name,anchor,vals);
		}
	}

	@Override
	public void ack(Tuple anchor) {
		this.collector.ack(anchor);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return conf;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		this.router.declareOutputFields(declarer);
	}

}