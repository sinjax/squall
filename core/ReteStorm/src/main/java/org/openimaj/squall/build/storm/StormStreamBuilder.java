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
import org.openimaj.squall.build.storm.topology.LocalClusterOperation;
import org.openimaj.squall.build.storm.topology.LocalClusterOperationTOF;
import org.openimaj.squall.build.storm.topology.TopologyOperationFactory;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.pair.IndependentPair;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * From an {@link OrchestratedProductionSystem} construct and deploy a storm topology 
 * based instantiation of this production system
 *
 */
public class StormStreamBuilder implements Builder{
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
		try{
			Set<NamedNode<?>> rootset = new HashSet<>();
			rootset.addAll(ops.root);
			TopologyBuilder tb = new TopologyBuilder();
			try {
				buildTopology(tb,ops,new HashMap<String,NamedNode<?>>(),rootset);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			StormTopology top = tb.createTopology();
			topop.perform(top);
		} catch (Throwable e){
			e.printStackTrace();
		}
		finally{			
			topop.cleanup();
		}
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
			String name = StormUtils.legalizeStormIdentifier(namedNode.getName());
			
			boolean remove = false;
			
			/**
			 * If it is a source, initialise the stream, grab it, 
			 * build a spout
			 */
			if(namedNode.isSource()){
				// build a spout
				IRichSpout s = new OIStreamSpout(namedNode);
				if (namedNode.forcedUnique()){
					tb.setSpout(name, s, 1);
				} else {
					tb.setSpout(name, s);
				}
				
				state.put(name, namedNode);
				remove = true;
			}
			/**
			 * If it is a function or an operation, build a bolt
			 * 
			 */
			else if(namedNode.isFunction() || namedNode.isOperation()){
				List<IndependentPair<NamedStream, NamedNode<?>>> parentStreams = extractParentStreams(ops,namedNode);
				IRichBolt funcBolt = null;
				
				if(namedNode.isFunction()){
					if (namedNode.isInitialisable()){
						funcBolt = new IFunctionBolt(namedNode);
					} else {
						funcBolt = new MultiFunctionBolt(namedNode);
					}
				} else { 
					funcBolt = new OperationBolt(namedNode);
				}
				
				BoltDeclarer dec;
				if (namedNode.forcedUnique()){
					dec = tb.setBolt(name, funcBolt, 1);
				} else {
					dec = tb.setBolt(name, funcBolt);
				}
				for (IndependentPair<NamedStream, NamedNode<?>> p : parentStreams) {
					NamedStream strm = p.firstObject();
					NamedNode<?> parent = p.secondObject();
					String parentName = StormUtils.legalizeStormIdentifier(parent.getName());
					String streamName = StormUtils.legalizeStormIdentifier(strm.identifier());
					if(namedNode.forcedUnique()){
						dec.globalGrouping(parentName, streamName);
					} else if(namedNode.isStateless()){
						dec.shuffleGrouping(parentName, streamName);
					} else if(strm.varCount() < 1){
						dec.allGrouping(parentName, streamName);
					} else{							
						dec.customGrouping(parentName, streamName, new ContextVariableGrouping(strm.variables()));
					}
				}
				state.put(name, namedNode);
				remove = true;
			}
			if(!remove){
				newdisconnected.add(namedNode);
			}
			else{
				// add the children!
				for (NamedStream edge : namedNode.childEdges()){
					for (NamedNode<?> child : edge.destinations()) {
						if(!disconnected.contains(child) && !state.containsKey(StormUtils.legalizeStormIdentifier(child.getName())))
						{
							newdisconnected.add(child);
						}
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
			for (NamedNode<?> nn : edge.sources()){
				IndependentPair<NamedStream, NamedNode<?>> pair = new IndependentPair<NamedStream, NamedNode<?>>(edge, nn);
				ret.add(pair);
			}
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
		
		for (NamedStream edge : namedNode.parentEdges()){
			for (NamedNode<?> par : edge.sources()) {
				if(!state.containsKey(StormUtils.legalizeStormIdentifier(par.getName()))){
					return false;
				}
			}
		}
		return true;
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
		conf.put(LocalClusterOperation.SLEEPKEY, sleep);
		return new StormStreamBuilder(LocalClusterOperationTOF.instance(), conf);
	}

}
