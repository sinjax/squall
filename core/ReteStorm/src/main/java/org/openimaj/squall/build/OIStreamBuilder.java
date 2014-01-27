package org.openimaj.squall.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.data.NonBlockingStream;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.stream.NullCatch;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.StreamLoopGuard;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * From an {@link OrchestratedProductionSystem} construct and run an OpenIMAJ {@link Stream} based 
 * instantiation of the production system
 *
 */
public class OIStreamBuilder implements Builder{
	/**
	 * 
	 */
	public OIStreamBuilder() {
	}

	@Override
	public void build(OrchestratedProductionSystem ops) {
		Set<NamedNode<?>> rootset = new HashSet<>();
		rootset.addAll(ops.root);
		if(ops.reentrant!=null){			
			rootset.add(ops.reentrant);
		}
		buildStream(ops,new HashMap<String,Stream<Context>>(),rootset);
	}

	private void buildStream(OrchestratedProductionSystem ops, Map<String, Stream<Context>> state, Set<? extends NamedNode<?>> disconnected) {
		
		/**
		 * Once there is only 1 disconnected node and that node has no children, we're at the end of the production system.
		 * By defenition this must be the NamedNode which holds the Operation so start the forEach here
		 * 
		 */
		if(disconnected.size() == 1 && disconnected.iterator().next().outgoingEdgeCount() == 0){
			NamedNode<?> last = disconnected.iterator().next();
			if(last.isInitialisable()){
				Initialisable init = last.getInit();
				init.setup();
			}
			
			if(!containsAllParents(state,last) || !last.isOperation()){
				throw new RuntimeException("Could not build stream: Multiple final terminals is bad and shouldn't ever happen");
			}
			List<Stream<Context>> parents = extractParentStreams(ops,state, last);
			Stream<Context> stream = null;
			if(parents.size() == 1){
				stream = parents.get(0);
			}
			else{
				stream = new JoinStream<Context>(parents);
			}
			stream.forEach(last.getOperation());
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
			 * If it is the reentrant source, it will be connected to later, so connect it as
			 * an empty joinable and hold on to that joinable's reference
			 */
			if(namedNode.isReentrantSource()){
				if(namedNode.isInitialisable()){
					Initialisable init = namedNode.getInit();
					init.setup();
				}
				Stream<Context> reentrantStream = new SplitStream<Context>(new JoinStream<Context>());
//				reentrantStream.map(namedNode.getFunction());
				state.put(name, reentrantStream);
				remove = true;
			}
			/**
			 * If it is a source, initialise the stream, grab it, put it in a 
			 * NonBlockingStream and if it has multiple children wrap it in a 
			 * SplitStream. Also note, we put a NullCatch around the NonBlockingStream
			 * 
			 * Is this better than expecting every function down the production system to handle nulls?
			 */
			else if(namedNode.isSource()){
				if(namedNode.isInitialisable()){
					Initialisable init = namedNode.getInit();
					init.setup();
				}
				
				Stream<Context> source = new NonBlockingStream<Context>(namedNode.getSource().apply()).map(new NullCatch<Context>());
				if(namedNode.outgoingEdgeCount() > 1){
					source = new SplitStream<Context>(source);
				}
				state.put(name, source);
				remove = true;
			}
			/**
			 * If it is a function and all its parents are ready, apply the function
			 * to the parents. If there are more than one parent wrap them up
			 * with a JoinStream and map this function to that join.
			 * 
			 */
			else if(namedNode.isFunction()){
				if(containsAllParents(state,namedNode)){
					// ready for connection!
					List<Stream<Context>> parents = extractParentStreams(ops,state,namedNode);
					if(namedNode.isInitialisable()){
						Initialisable init = namedNode.getInit();
						init.setup();
					}
					Stream<Context> funStream;
					MultiFunction<Context, Context> fun = namedNode.getFunction();
					if(parents.size() > 1) {
						funStream = new JoinStream<Context>(parents).map(fun);
					} else {
						funStream = parents.get(0).map(fun);				
					}
					if(namedNode.outgoingEdgeCount() > 1){
						funStream = new SplitStream<Context>(funStream);
					}
					
					state.put(name, funStream);
					remove = true;
				}
			}
			if(!remove){
				newdisconnected.add(namedNode);
			}
			else{
				// add the children!
				for (NamedStream outStream : namedNode.childEdges()){
					for (NamedNode<?> child : outStream.destinations()) {
						if(child.isReentrantSource()){
							// If the child is the reentrant source, it must already be added, you must add the parent to the child
							Stream<Context> reentrantSource = state.get(child.getName());
							// The reentrant source is a SplitStream which contains a join, this node must be added to that join!
							JoinStream<Context> strm = (JoinStream<Context>) ((SplitStream<Context>)reentrantSource).getInnerStream();
							Stream<Context> currentStream = state.get(name);
							MultiFunction<Context, Context> reentrantFun = child.getFunction();
							Stream<Context> mapped = currentStream.map(reentrantFun);
							Stream<Context> loopGuarded = new StreamLoopGuard<Context>(mapped);
//							strm.addStream(loopGuarded);
						}
						else{						
							newdisconnected.add(child);
						}
					}
				}
			}
		}
		buildStream(ops,state,newdisconnected);
	}

	/**
	 * Grab all the parent streams, attaching them to a {@link NamedStream} function on the way
	 * @param ops
	 * @param state
	 * @param namedNode
	 * @return
	 */
	private List<Stream<Context>> extractParentStreams(OrchestratedProductionSystem ops, Map<String, Stream<Context>> state, NamedNode<?> namedNode) {
		List<Stream<Context>> ret = new ArrayList<Stream<Context>>();
		for (NamedStream edge : namedNode.parentEdges()) {
			Function<Context, Context> edgeFunction = edge.getFunction();
			for (NamedNode<?> node : edge.sources()){
				String sourceName = node.getName();
				Stream<Context> sourceStream = state.get(sourceName);
				ret.add(sourceStream.map(edgeFunction));
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
	private boolean containsAllParents(Map<String, Stream<Context>> state,NamedNode<?> namedNode) {
		
		for (NamedStream edge : namedNode.parentEdges()){
			for (NamedNode<?> par : edge.sources()) {
				if(!state.containsKey(par.getName())){
					return false;
				}
			}
		}
		return true;
	}

}
