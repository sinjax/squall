package org.openimaj.squall.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.build.utils.TripleContenxtWrapper;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.data.NonBlockingStream;
import org.openimaj.util.function.Function;
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

	public OIStreamBuilder() {
	}

	@Override
	public void build(OrchestratedProductionSystem ops) {
		Stream<Context> answer = buildStream(ops.root);
	}

	private Stream<Context> buildStream(List<NamedSourceNode> root) {
		Map<String,Stream<Context>> state = new HashMap<String,Stream<Context>>();
		List<NamedNode<?>> disconnected = new ArrayList<NamedNode<?>>();
		for (NamedSourceNode namedSourceNode : root) {
			IStream<Context> source = namedSourceNode.getData();
			source.setup();
			state.put(namedSourceNode.getName(), new NonBlockingStream<Context>(source));
			for (Iterator<NamedNode<?>> iterator = namedSourceNode.childiterator(); iterator.hasNext();) {
				disconnected.add(iterator.next());
			}
		}
		return buildStream(state,disconnected); 
	}

	private Stream<Context> buildStream(Map<String, Stream<Context>> state, List<NamedNode<?>> disconnected) {
		
		if(disconnected.size() == 1 && disconnected.get(0).childCount() == 0){
			NamedNode<?> last = disconnected.get(0);
			Stream<Context> lastStream = null;
			if(last.isInitialisable()){
				Initialisable init = last.getInit();
				init.setup();
			}
			
			if(!containsAllParents(state,last)){
				throw new RuntimeException("Could not build stream: Multiple final terminals is bad and shouldn't ever happen");
			}
			return lastStream;
		}
		Iterator<NamedNode<?>> iter = disconnected.iterator();
		while(iter.hasNext()){
			NamedNode<?> namedNode = iter.next();
			String name = namedNode.getName();
			
			boolean remove = false;
			
			if(namedNode.isSource()){
				if(namedNode.isInitialisable()){
					Initialisable init = namedNode.getInit();
					init.setup();
				}
				Stream<Context> source = namedNode.getSource();
				state.put(name, new NonBlockingStream<Context>(source));
				remove = true;
			}
			if(namedNode.isFunction()){
				if(containsAllParents(state,namedNode)){
					// ready for connection!
					List<Stream<Context>> parents = extractParentStreams(state,namedNode);
					if(namedNode.isInitialisable()){
						Initialisable init = namedNode.getInit();
						init.setup();
					}
					Stream<Context> funStream;
					Function<Context, Context> fun = namedNode.getFunction();
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
		}
		return buildStream(state,disconnected);
	}

	private List<Stream<Context>> extractParentStreams(Map<String, Stream<Context>> state, NamedNode<?> namedNode) {
		return null;
	}

	private boolean containsAllParents(Map<String, Stream<Context>> state,NamedNode<?> namedNode) {
		return false;
	}

}
