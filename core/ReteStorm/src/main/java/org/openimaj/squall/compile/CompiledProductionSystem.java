package org.openimaj.squall.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class CompiledProductionSystem {
	
	/**
	 * A stream of triples is the source of our production systems
	 */
	List<ISource<Stream<Context>>> sources;
	
	/**
	 * List of production systems that this compilation is made from
	 */
	List<CompiledProductionSystem> systems;	
	/**
	 * Filters match triples and assign variables to values within the triple.
	 */
	List<JoinComponent<?>> joinlist;
	
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
		sources = new ArrayList<ISource<Stream<Context>>>();
		systems = new ArrayList<CompiledProductionSystem>();
		joinlist = new ArrayList<JoinComponent<?>>();
		predicates = new ArrayList<IVFunction<Context, Context>>();
		aggregations = new ArrayList<IVFunction<List<Context>, Context>>();
		consequence = null;
	}
	
	/**
	 * a source of INPUT
	 * @param stream
	 * @return a {@link Stream} of input 
	 */
	public CompiledProductionSystem addSource(ISource<Stream<Context>> stream) {
		this.sources.add(stream);
		return this;
	}
	
	/**
	 * Add a system as a part of this production system, this system is added to the 0th {@link List} of {@link CompiledProductionSystem}
	 * @param sys
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addSystem(CompiledProductionSystem sys){
		systems.add(sys);
		return this;
	}
	
	/**
	 * Add a filter function. Filters consume {@link Triple}, decide whether they
	 * concern the filter, and if so emit a {@link Map} of bindings, otherwise null.
	 * @param filter
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addJoinComponent(JoinComponent<?> filter){
		joinlist.add(filter);
		return this;
	}
	
	/**
	 * @param filter add this filter as a {@link JoinComponent}
	 */
	public void addJoinComponent(IVFunction<Context,Context> filter) {
		this.addJoinComponent(new JoinComponent.IVFunctionJoinComponent(filter));
	}
	
	/**
	 * @param filter add this filter as a {@link JoinComponent}
	 */
	public void addJoinComponent(CompiledProductionSystem filter) {
		this.addJoinComponent(new JoinComponent.CPSJoinComponent(filter));
	}
	
	

	/**
	 * Add a predicate. Predicates consume bindings and decide whether they pass
	 * a given filter function
	 * @param predicate
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addPredicate(IVFunction<Context,Context> predicate){
		this.predicates.add(predicate);
		return this;
	}
	
	/**
	 * @param item
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem setConsequence(IVFunction<Context, Context> item){
		this.consequence = item;
		return this;
	}

	/**
	 * @return the filters of this system
	 */
	public List<JoinComponent<?>> getJoinComponents() {
		return this.joinlist;
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
	public List<CompiledProductionSystem> getSystems() {
		return this.systems;
	}

	/**
	 * @return the consequences of this compiled system
	 */
	public IVFunction<Context, Context> getConsequence() {
		return this.consequence;
	}

	/**
	 * @return the sources
	 */
	public List<ISource<Stream<Context>>> getSources() {
		return this.sources;
	}

	/**
	 * @return the bindings aggregations
	 */
	public List<IVFunction<List<Context>, Context>> getAggregations() {
		return this.aggregations;
	}
	
}