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
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * From an {@link OrchestratedProductionSystem} construct an OpenIMAJ {@link Stream} based 
 * instantiation
 *
 */
public class OIStreamBuilder implements Builder{

	private CollectionStream<Triple> triples;

	/**
	 * 
	 */
	public OIStreamBuilder() {
	}

	@Override
	public void build(OrchestratedProductionSystem ops) {
		Set<NamedNode<?>> rootset = new HashSet<>();
		rootset.addAll(ops.root);
		buildStream(ops,new HashMap<String,Stream<Context>>(),rootset);
	}

	private void buildStream(OrchestratedProductionSystem ops, Map<String, Stream<Context>> state, Set<? extends NamedNode<?>> disconnected) {
		
		if(disconnected.size() == 1 && disconnected.iterator().next().childCount() == 0){
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
		Iterator<? extends NamedNode<?>> iter = disconnected.iterator();
		Set<NamedNode<?>> newdisconnected = new HashSet<NamedNode<?>>();
		while(iter.hasNext()){
			NamedNode<?> namedNode = iter.next();
			String name = namedNode.getName();
			
			boolean remove = false;
			
			if(namedNode.isSource()){
				if(namedNode.isInitialisable()){
					Initialisable init = namedNode.getInit();
					init.setup();
				}
				Stream<Context> source = new NonBlockingStream<Context>(namedNode.getSource());
				if(namedNode.childCount() > 1){
					source = new SplitStream<Context>(source);
				}
				state.put(name, source);
				remove = true;
			}
			if(namedNode.isFunction()){
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
					if(namedNode.childCount() > 1){
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
				for (NamedNode<?> child : namedNode.children()) {
					newdisconnected.add(child);
				}
			}
		}
		buildStream(ops,state,newdisconnected);
	}

	private List<Stream<Context>> extractParentStreams(OrchestratedProductionSystem ops, Map<String, Stream<Context>> state, NamedNode<?> namedNode) {
		List<Stream<Context>> ret = new ArrayList<Stream<Context>>();
		for (NamedStream edge : namedNode.parentEdges()) {
			ret.add(state.get(ops.getEdgeSource(edge).getName()).map(edge.getFunction()));
		}
		return ret;
	}

	private boolean containsAllParents(Map<String, Stream<Context>> state,NamedNode<?> namedNode) {
		
		for (NamedNode<?> par : namedNode.parents()) {
			if(!state.containsKey(par.getName())){
				return false;
			}
		}
		return true;
	}

}
