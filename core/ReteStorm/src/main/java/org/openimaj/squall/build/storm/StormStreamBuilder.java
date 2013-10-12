package org.openimaj.squall.build.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.squall.build.Builder;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

import backtype.storm.generated.StormTopology;
import backtype.storm.scheduler.Cluster;
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

	private Operation<StormTopology> topop;

	/**
	 * @param topop
	 */
	private StormStreamBuilder(Operation<StormTopology> topop) {
		this.topop = topop;
	}

	@Override
	public void build(OrchestratedProductionSystem ops) {
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
			if(namedNode.childCount() == 0) continue; // this is the last node! deal with it later.
			String name = namedNode.getName();
			
			boolean remove = false;
			
			/**
			 * If it is a source, initialise the stream, grab it, 
			 * build a spout
			 */
			if(namedNode.isSource()){
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
			if(namedNode.isFunction() || namedNode.isOperation()){
				if(containsAllParents(state,namedNode)){
					List<IndependentPair<NamedStream, NamedNode<?>>> parentStreams = extractParentStreams(ops,state,namedNode);
					IRichBolt funcBolt = null;
					
					if(namedNode.isFunction())
						funcBolt = new MultiFunctionBolt(namedNode);
					else 
						funcBolt = new OperationBolt(namedNode);
					
					BoltDeclarer dec = tb.setBolt(name, funcBolt);
					for (IndependentPair<NamedStream, NamedNode<?>> p : parentStreams) {
						NamedStream strm = p.firstObject();
						NamedNode<?> parent = p.secondObject();
						if(strm.variables().size() == 0){
							dec.shuffleGrouping(parent.getName(), strm.getName());
						}
						else{							
							dec.customGrouping(parent.getName(), strm.getName(), new ContextVariableGrouping(strm.variables()));
						}
					}
					remove = true;
				}
			}
			if(!remove){
				newdisconnected.add(namedNode);
			}
			else{
				// add the children!
				for (NamedNode<?> child : namedNode.children()) {
					newdisconnected.add(child);
				}
			}
		}
		buildTopology(tb, ops,state,newdisconnected);
	}

	/**
	 * Grab all the parent streams, attaching them to a {@link NamedStream} function on the way
	 * @param ops
	 * @param state
	 * @param namedNode
	 * @return
	 */
	private List<IndependentPair<NamedStream, NamedNode<?>>> extractParentStreams(OrchestratedProductionSystem ops, Map<String, NamedNode<?>> state, NamedNode<?> namedNode) {
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

}
