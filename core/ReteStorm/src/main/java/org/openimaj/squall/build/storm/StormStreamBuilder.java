package org.openimaj.squall.build.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.build.Builder;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.StreamLoopGuard;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * From an {@link OrchestratedProductionSystem} construct and deploy a storm topology 
 * based instantiation of this production system
 *
 */
public class StormStreamBuilder implements Builder{

	private static final String SLEEPKEY = "org.openimaj.squall.build.storm.sleep";
	private Config conf;
	private TopologyOperationFactory topopf;

	/**
	 * @param localClusterOperationTOF
	 * @param conf 
	 */
	public StormStreamBuilder(TopologyOperationFactory localClusterOperationTOF, Config conf) {
		
		this.topopf = localClusterOperationTOF;
		this.conf = conf;
	}

	@Override
	public void build(OrchestratedProductionSystem ops) {
		IOperation<StormTopology> topop = topopf.topop(conf);
		topop.setup();
		Set<NamedNode<?>> rootset = new HashSet<>();
		rootset.addAll(ops.root);
		if(ops.reentrant!=null){			
			rootset.add(ops.reentrant);
		}
		TopologyBuilder tb = new TopologyBuilder();
		try {
			buildTopology(tb,ops,new HashMap<String,NamedNode<?>>(),rootset);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		StormTopology top = tb.createTopology();
		topop.perform(top);
		topop.cleanup();
	}

	private void buildTopology(TopologyBuilder tb, OrchestratedProductionSystem ops, Map<String, NamedNode<?>> state, Set<? extends NamedNode<?>> disconnected) throws Exception {
		
		/**
		 * Once there are no disconnected nodes, we're done!
		 * 
		 */
		if(disconnected.size() == 0){
			return;
		}
		/**
		 * Try to connect each currently disconnected node
		 */
		Iterator<? extends NamedNode<?>> iter = disconnected.iterator();
		Set<NamedNode<?>> newdisconnected = new HashSet<NamedNode<?>>();
		while(iter.hasNext()){
			NamedNode<?> namedNode = iter.next();
			String name = namedNode.getName();
			
			boolean remove = false;
			
			/**
			 * If it is a source, initialise the stream, grab it, 
			 * build a spout
			 */
			if(namedNode.isReentrantSource()){
				// build a spout
				MultiFunctionBolt funcBolt = new MultiFunctionBolt(namedNode);
				BoltDeclarer dec = tb.setBolt(name, funcBolt);
				List<IndependentPair<NamedStream, NamedNode<?>>> parents = extractParentStreams(ops, namedNode);
				for (IndependentPair<NamedStream, NamedNode<?>> p : parents) {
					NamedStream strm = p.firstObject();
					NamedNode<?> parent = p.secondObject();
					String streamName = NamedNodeComponent.constructStreamName(parent,strm,namedNode);
					dec.shuffleGrouping(parent.getName(), streamName);
				}
				state.put(name, namedNode);
				remove = true;
			}
			/**
			 * If it is a source, initialise the stream, grab it, 
			 * build a spout
			 */
			else if(namedNode.isSource()){
				// build a spout
				IRichSpout s = new OIStreamSpout(namedNode);
				tb.setSpout(name, s);
				
				state.put(name, namedNode);
				remove = true;
			}
			/**
			 * If it is a function and all its parents are ready, build a bolt
			 * 
			 */
			else if(namedNode.isFunction() || namedNode.isOperation()){
				if(containsAllParents(state,namedNode)){
					List<IndependentPair<NamedStream, NamedNode<?>>> parentStreams = extractParentStreams(ops,namedNode);
					IRichBolt funcBolt = null;
					
					if(namedNode.isFunction())
						funcBolt = new MultiFunctionBolt(namedNode);
					else 
						funcBolt = new OperationBolt(namedNode);
					
					BoltDeclarer dec = tb.setBolt(name, funcBolt);
					for (IndependentPair<NamedStream, NamedNode<?>> p : parentStreams) {
						NamedStream strm = p.firstObject();
						NamedNode<?> parent = p.secondObject();
						String streamName = NamedNodeComponent.constructStreamName(parent,strm,namedNode);
						if(strm.variables() == null){
							dec.shuffleGrouping(parent.getName(), streamName);
						}
						else if(strm.variables().size() == 0){
							dec.allGrouping(parent.getName(), streamName);
						}
						else{							
							dec.customGrouping(parent.getName(), streamName, new ContextVariableGrouping(strm.variables()));
						}
					}
					state.put(name, namedNode);
					remove = true;
				}
			}
			if(!remove){
				newdisconnected.add(namedNode);
			}
			else{
				// add the children!
				for (NamedNode<?> child : namedNode.children()) {
					if(!disconnected.contains(child) && !state.containsKey(child.getName()))
					{
						newdisconnected.add(child);
					}
				}
			}
		}
		buildTopology(tb, ops,state,newdisconnected);
	}

	/**
	 * Grab all the parent streams, attaching them to a {@link NamedStream} function on the way
	 * @param ops
	 * @param namedNode
	 * @return
	 */
	private List<IndependentPair<NamedStream, NamedNode<?>>> extractParentStreams(OrchestratedProductionSystem ops, NamedNode<?> namedNode) {
		List<IndependentPair<NamedStream, NamedNode<?>>> ret = new ArrayList<IndependentPair<NamedStream, NamedNode<?>>>();
		for (NamedStream edge : namedNode.parentEdges()) {
			IndependentPair<NamedStream, NamedNode<?>> pair = new IndependentPair<NamedStream, NamedNode<?>>(edge, ops.getEdgeSource(edge));
			ret.add(pair);
		}
		return ret;
	}

	/**
	 * Have all the parents of a node been created?
	 * @param state
	 * @param namedNode
	 * @return
	 */
	private boolean containsAllParents(Map<String, NamedNode<?>> state,NamedNode<?> namedNode) {
		
		for (NamedNode<?> par : namedNode.parents()) {
			if(!state.containsKey(par.getName())){
				return false;
			}
		}
		return true;
	}

	static class LocalClusterOperation implements IOperation<StormTopology>{		
		long DEFAULT_SLEEP_TIME = 5000;
		
		private Config conf;
		private String name = "local";

		private LocalCluster cluster;

		public LocalClusterOperation(Config conf) {
			this.conf = conf;
			this.conf.put(Config.STORM_LOCAL_MODE_ZMQ, true);
		}

		@Override
		public void perform(StormTopology object) {
			cluster.submitTopology(name, conf, object);
		}

		@Override
		public void setup() {
			this.cluster = new LocalCluster();
			JenaStormUtils.registerSerializers(conf);
			conf.setFallBackOnJavaSerialization(true);
		}

		@Override
		public void cleanup() {
			long sleepTime = (Long) conf.get(SLEEPKEY);
			try {
				if (sleepTime < 0) {
					while (true) {
						Utils.sleep(DEFAULT_SLEEP_TIME);
					}
				} else {
					Utils.sleep(sleepTime);

				}
			} finally {
				cluster.killTopology(name);
//				cluster.shutdown();
			}
			
		}
		
	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public abstract static class TopologyOperationFactory {
		/**
		 * @param conf
		 * @return an operation which handles a {@link StormTopology}
		 */
		public abstract IOperation<StormTopology> topop(Config conf);
	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class LocalClusterOperationTOF extends TopologyOperationFactory{

		private static LocalClusterOperationTOF instance = null;

		@Override
		public IOperation<StormTopology> topop(Config conf) {
			return new LocalClusterOperation(conf);
		}

		/**
		 * @return the instance of this factory
		 */
		public static LocalClusterOperationTOF instance() {
			if(instance == null){
				instance  = new LocalClusterOperationTOF();
			}
			return instance;
		}
		
	}
	/**
	 * Calls {@link #localClusterBuilder(long)} with -1 (sleep forever)
	 * @return build a {@link StormStreamBuilder} deployed on a local cluster
	 */
	public static StormStreamBuilder localClusterBuilder() {
		return localClusterBuilder(-1);
	}
	/**
	 * 
	 * @param sleep how long to sleep
	 * @return build a {@link StormStreamBuilder} deployed on a local cluster
	 */
	public static StormStreamBuilder localClusterBuilder(long sleep) {
		Config conf = new Config();
		JenaStormUtils.registerSerializers(conf);
		conf.put(SLEEPKEY, sleep);
		return new StormStreamBuilder(LocalClusterOperationTOF.instance(), conf);
	}

}
