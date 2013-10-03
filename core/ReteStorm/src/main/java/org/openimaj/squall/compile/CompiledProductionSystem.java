package org.openimaj.squall.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.compile.data.ComponentInformationPredicate;
import org.openimaj.squall.data.ComponentInformation;
import org.openimaj.util.function.Function;

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
 */
public class CompiledProductionSystem {
	
	
	/**
	 * FIXME: The compiler should have sources
	 */
	
	/**
	 * List of production systems that this compilation is made from
	 */
	List<CompiledProductionSystem> systems;	
	/**
	 * Filters match triples and assign variables to values within the triple.
	 */
	List<ComponentInformationFunction<Triple,Map<String,String>>> filters;
	
	/**
	 * Predicates confirm or deny certain bindings. Empty means no predicates
	 * FIXME: Make this into a {@link ComponentInformationFunction} of bindings
	 */
	List<ComponentInformationPredicate<Map<String, String>>> predicates;
	
	/**
	 * Groups suggest a join order for filters and binding predicates. Empty means no preffered order
	 */
	List<CompiledProductionSystem> groups;
	
	/**
	 * Aggregations consume lists of bindings and produce bindings. Empty means no aggregations
	 */
	List<Function<List<Map<String,String>>,Map<String,String>>> aggregations;
	
	/**
	 * Consequences consume bindings and perform some operation
	 */
	List<Function<Map<String,String>,?>> consequences;
	
	private ComponentInformation information;
	/**
	 * Initialise all system parts as empty, a fairly boring production system
	 */
	public CompiledProductionSystem() {
		systems = new ArrayList<CompiledProductionSystem>();
		filters = new ArrayList<ComponentInformationFunction<Triple, Map<String, String>>>();
		predicates = new ArrayList<ComponentInformationPredicate<Map<String, String>>>();
		groups = new ArrayList<CompiledProductionSystem>();
		aggregations = new ArrayList<Function<List<Map<String,String>>,Map<String,String>>>();
		consequences = new ArrayList<Function<Map<String,String>,?>>();
	}
	
	/**
	 * Add a system as a part of this production system
	 * @param sys
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addSystem(CompiledProductionSystem sys){
		this.systems.add(sys);
		return this;
	}
	
	/**
	 * Add a filter function. Filters consume {@link Triple}, decide whether they
	 * concern the filter, and if so emit a {@link Map} of bindings, otherwise null.
	 * @param filter
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addFilter(ComponentInformationFunction<Triple, Map<String, String>> filter){
		this.filters.add(filter);
		return this;
	}
	
	/**
	 * Add a predicate. Predicates consume bindings and decide whether they pass
	 * a given filter function
	 * @param predicate
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addPredicate(ComponentInformationPredicate<Map<String, String>> predicate){
		this.predicates.add(predicate);
		return this;
	}
	
	/**
	 * Specifiy a group
	 * @param comp 
	 * @return return this system (useful for chaining) 
	 */
	public CompiledProductionSystem addGroup(CompiledProductionSystem comp){
		this.groups.add(comp);
		return this;
	}
	
	/**
	 * Specify an aggregation 
	 * @param aggr 
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addAggregation(CompiledProductionSystem aggr){
		this.groups.add(aggr);
		return this;
	}
	
	/**
	 * @param item
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addConsequence(Function<Map<String, String>, ?> item){
		this.consequences.add(item);
		return this;
	}

	/**
	 * @return the filters of this system
	 */
	public List<ComponentInformationFunction<Triple, Map<String, String>>> getFilters() {
		return this.filters;
	}

	/**
	 * @return the predicates of this system
	 */
	public List<ComponentInformationPredicate<Map<String, String>>> getPredicates() {
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
	public List<Function<Map<String, String>, ?>> getConequences() {
		return this.consequences;
	}

	public ComponentInformation information() {
		return information;
	}
}