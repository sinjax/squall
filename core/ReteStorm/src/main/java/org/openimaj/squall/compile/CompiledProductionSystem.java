package org.openimaj.squall.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.util.function.Function;
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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <INPUT> The sources of the production system produce this
 * @param <OUTPUT> The consequences of this production system produce  this
 */
public abstract class CompiledProductionSystem<INPUT,OUTPUT> {
	
	
	/**
	 * A stream of triples is the source of ourproduction systems
	 */
	List<Stream<INPUT>> sources;
	
	/**
	 * List of production systems that this compilation is made from
	 */
	List<CompiledProductionSystem<INPUT,OUTPUT>> systems;	
	/**
	 * Filters match triples and assign variables to values within the triple.
	 */
	List<VariableFunction<INPUT,Map<String,String>>> filters;
	
	/**
	 * Predicates confirm or deny certain bindings. Empty means no predicates
	 * FIXME: Make this into a {@link VariableFunction} of bindings
	 */
	List<VariableFunction<Map<String, String>,Map<String, String>>> predicates;
	
	/**
	 * Groups suggest a join order for filters and binding predicates. Empty means no preffered order
	 */
	List<CompiledProductionSystem<INPUT,OUTPUT>> groups;
	
	/**
	 * Aggregations consume lists of bindings and produce bindings. Empty means no aggregations
	 */
	List<Function<List<Map<String,String>>,Map<String,String>>> aggregations;
	
	/**
	 * Consequences consume bindings and perform some operation
	 */
	List<Function<Map<String,String>,OUTPUT>> consequences;
	/**
	 * Initialise all system parts as empty, a fairly boring production system
	 */
	public CompiledProductionSystem() {
		systems = new ArrayList<CompiledProductionSystem<INPUT,OUTPUT>>();
		filters = new ArrayList<VariableFunction<INPUT, Map<String, String>>>();
		predicates = new ArrayList<VariableFunction<Map<String, String>, Map<String, String>>>();
		groups = new ArrayList<CompiledProductionSystem<INPUT,OUTPUT>>();
		aggregations = new ArrayList<Function<List<Map<String,String>>,Map<String,String>>>();
		consequences = new ArrayList<Function<Map<String,String>,OUTPUT>>();
	}
	
	/**
	 * Add a system as a part of this production system
	 * @param sys
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addSystem(CompiledProductionSystem<INPUT,OUTPUT> sys){
		this.systems.add(sys);
		return this;
	}
	
	/**
	 * Add a filter function. Filters consume {@link Triple}, decide whether they
	 * concern the filter, and if so emit a {@link Map} of bindings, otherwise null.
	 * @param filter
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addFilter(VariableFunction<INPUT, Map<String, String>> filter){
		this.filters.add(filter);
		return this;
	}
	
	/**
	 * Add a predicate. Predicates consume bindings and decide whether they pass
	 * a given filter function
	 * @param predicate
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addPredicate(VariableFunction<Map<String, String>, Map<String, String>> predicate){
		this.predicates.add(predicate);
		return this;
	}
	
	/**
	 * Specifiy a group
	 * @param comp 
	 * @return return this system (useful for chaining) 
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addGroup(CompiledProductionSystem<INPUT,OUTPUT> comp){
		this.groups.add(comp);
		return this;
	}
	
	/**
	 * @param item
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> addConsequence(Function<Map<String, String>, OUTPUT> item){
		this.consequences.add(item);
		return this;
	}

	/**
	 * @return the filters of this system
	 */
	public List<VariableFunction<INPUT, Map<String, String>>> getFilters() {
		return this.filters;
	}

	/**
	 * @return the predicates of this system
	 */
	public List<VariableFunction<Map<String, String>, Map<String, String>>> getPredicates() {
		return this.predicates;
	}

	/**
	 * @return the sub systems of this {@link CompiledProductionSystem}
	 */
	public List<CompiledProductionSystem<INPUT,OUTPUT>> getSystems() {
		return this.systems;
	}

	/**
	 * @return the consequences of this compiled system
	 */
	public List<Function<Map<String, String>, OUTPUT>> getConequences() {
		return this.consequences;
	}
}