package org.openimaj.squall.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;


/**
 * 
 * A generalised production system can be thought of as a set of production systems, filters and predicates that must
 * all pass and be joined together in an "AND" like fasion. Once done so, the aggregations should be consulted
 * and finally the consequences should occur.
 * 
 * A {@link CompiledProductionSystem} may also suggest groupings of the "AND" parts in the form 
 * of a list of {@link CompiledProductionSystem}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <INPUT> The sources of the production system produce this
 * @param <OUTPUT> The consequences of this production system produce  this
 */
public abstract class CompiledProductionSystem<INPUT,OUTPUT> {
	
	
	/**
	 * A stream of triples is the source of ourproduction systems
	 */
	List<IStream<Context>> sources;
	
	/**
	 * List of production systems that this compilation is made from
	 */
	List<List<CompiledProductionSystem<Context,Context>>> systems;	
	/**
	 * Filters match triples and assign variables to values within the triple.
	 */
	List<IVFunction<Context,Context>> filters;
	
	/**
	 * Predicates confirm or deny certain bindings. Empty means no predicates
	 */
	List<IVFunction<Context,Context>> predicates;
	
	
	/**
	 * Aggregations consume lists of bindings and produce bindings. Empty means no aggregations
	 */
	List<IVFunction<List<Context>, Context>> aggregations;
	 
	/**
	 * Consequences consume bindings and perform some operation
	 */
	IVFunction<Context, Context> consequence;
	
	/**
	 * Initialise all system parts as empty, a fairly boring production system
	 */
	public CompiledProductionSystem() {
		sources = new ArrayList<IStream<Context>>();
		systems = new ArrayList<List<CompiledProductionSystem<Context, Context>>>();
		filters = new ArrayList<IVFunction<Context, Context>>();
		predicates = new ArrayList<IVFunction<Context, Context>>();
		aggregations = new ArrayList<IVFunction<List<Context>, Context>>();
		consequence = null;
	}
	
	/**
	 * a source of INPUT
	 * @param stream
	 * @return a {@link Stream} of input 
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addSource(IStream<Context> stream) {
		this.sources.add(stream);
		return this;
	}
	
	/**
	 * Add a system as a part of this production system, this system is added to the 0th {@link List} of {@link CompiledProductionSystem}
	 * @param sys
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addSystem(CompiledProductionSystem<Context,Context> sys){
		addFirst(systems,sys);
		return this;
	}
	
	/**
	 * Add a filter function. Filters consume {@link Triple}, decide whether they
	 * concern the filter, and if so emit a {@link Map} of bindings, otherwise null.
	 * @param filter
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addFilter(IVFunction<Context,Context> filter){
		filters.add(filter);
		return this;
	}
	
	private <T> void addFirst(List<List<T>> listlist,T item) {
		if(listlist.size() == 0){
			listlist.add(new ArrayList<T>());
		}
		listlist.get(0).add(item);
		
	}

	/**
	 * Add a predicate. Predicates consume bindings and decide whether they pass
	 * a given filter function
	 * @param predicate
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addPredicate(IVFunction<Context,Context> predicate){
		this.predicates.add(predicate);
		return this;
	}
	
	/**
	 * @param item
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> setConsequence(IVFunction<Context, Context> item){
		this.consequence = item;
		return this;
	}

	/**
	 * @return the filters of this system
	 */
	public List<IVFunction<Context, Context>> getFilters() {
		return this.filters;
	}

	/**
	 * @return the predicates of this system
	 */
	public List<IVFunction<Context, Context>> getPredicates() {
		return this.predicates;
	}

	/**
	 * @return the sub systems of this {@link CompiledProductionSystem}
	 */
	public List<List<CompiledProductionSystem<Context, Context>>> getSystems() {
		return this.systems;
	}

	/**
	 * @return the consequences of this compiled system
	 */
	public IVFunction<Context, Context> getConequences() {
		return this.consequence;
	}

	/**
	 * @return the sources
	 */
	public List<IStream<Context>> getSources() {
		return this.sources;
	}

	/**
	 * @return the bindings aggregations
	 */
	public List<IVFunction<List<Context>, Context>> getAggregations() {
		return this.aggregations;
	}
}