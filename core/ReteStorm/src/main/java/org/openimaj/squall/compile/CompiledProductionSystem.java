package org.openimaj.squall.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.OptionalProductionSystems;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;


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
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class CompiledProductionSystem {
	
	/**
	 * A list of triples provides the axioms of our production systems
	 */
	List<ClauseEntry> axioms;
	
	/**
	 * Streams of triples are the source of our production systems
	 */
	List<ISource<Stream<Context>>> streamSources;
	
	/**
	 * List of production systems that this compilation is made from
	 */
	List<OptionalProductionSystems> systems;	
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
	List<IVFunction<Context, Context>> consequences;
	
	/**
	 * Reentrant {@link CompiledProductionSystem} instance expect their Consequences to be re-consumed 
	 */
	boolean isReentrant = false;
	
	/**
	 * Initialise all system parts as empty, a fairly boring production system
	 */
	public CompiledProductionSystem() {
		axioms = new ArrayList<ClauseEntry>();
		streamSources = new ArrayList<ISource<Stream<Context>>>();
		systems = new ArrayList<OptionalProductionSystems>();
		joinlist = new ArrayList<JoinComponent<?>>();
		predicates = new ArrayList<IVFunction<Context, Context>>();
		aggregations = new ArrayList<IVFunction<List<Context>, Context>>();
		consequences = new ArrayList<IVFunction<Context,Context>>();
	}
	
	/**
	 * a statement of truth about the system
	 * @param axiom
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addAxiom(ClauseEntry axiom){
		this.axioms.add(axiom);
		return this;
	}
	
	/**
	 * some statements of truth about the system
	 * @param axioms
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addAxioms(Collection<ClauseEntry> axioms){
		this.axioms.addAll(axioms);
		return this;
	}
	
	/**
	 * a source of INPUT
	 * @param stream
	 * @return return this system (useful for chaining) 
	 */
	public CompiledProductionSystem addStreamSource(ISource<Stream<Context>> stream) {
		this.streamSources.add(stream);
		return this;
	}
	
	/**
	 * Add a system as a part of this production system, this system is added to the 0th {@link List} of {@link CompiledProductionSystem}
	 * @param sys
	 * @return return this system (useful for chaining)
	 */
	public CompiledProductionSystem addOption(OptionalProductionSystems sys){
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
	 * 
	 * @param reentrant whether the current {@link CompiledProductionSystem} should have its output re-entered into the system
	 * @return the current {@link CompiledProductionSystem}
	 */
	public CompiledProductionSystem setReentrant(boolean reentrant){
		this.isReentrant = reentrant;
		return this;
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
	public CompiledProductionSystem addConsequence(IVFunction<Context, Context> item){
		this.consequences.add(item);
		return this;
	}

	/**
	 * @return the filters of this system
	 */
	public List<JoinComponent<?>> getJoinComponents() {
		return this.joinlist;
	}
	
	public boolean isReentrant(){
		return this.isReentrant;
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
	public List<OptionalProductionSystems> getSystems() {
		return this.systems;
	}

	/**
	 * @return the consequences of this compiled system
	 */
	public List<IVFunction<Context, Context>> getConsequences() {
		return this.consequences;
	}

	/**
	 * @return the sources
	 */
	public List<ISource<Stream<Context>>> getStreamSources() {
		return this.streamSources;
	}
	
	/**
	 * Creates a new ISource, which takes the list of Axioms and produces a list of Contexts representing those Axioms.
	 * The ISource then takes the iterator over those Axiom Contexts as its Stream. 
	 * @return a source of Axioms
	 */
	public ISource<Stream<Context>> getAxiomSource() {
		return new ISource<Stream<Context>>(){

			private Context[] axioms;
			
			@Override
			public void setup() {}
			@Override
			public void cleanup() {}

			@Override
			public Stream<Context> apply() {
				return new AbstractStream<Context>(){

					private Iterator<Context> axiomIter;
					
					@Override
					public boolean hasNext() {
						return axiomIter.hasNext();
					}

					@Override
					public Context next() {
						return axiomIter.next();
					}
					
					public Stream<Context> setAxiomIter (Context[] axioms){
						this.axiomIter = Arrays.asList(axioms).iterator();
						return this;
					}
					
				}.setAxiomIter(this.axioms);
			}

			@Override
			public Stream<Context> apply(Stream<Context> in) {
				return this.apply();
			}
			
			public ISource<Stream<Context>> setAxiomList(List<ClauseEntry> as){
				this.axioms = new Context[as.size()];
				
				int i;
				for (ClauseEntry axiom = as.get(i = 0); i < as.size(); axiom = as.get(++i)){
					Context ac = new Context();
					if (axiom instanceof TriplePattern){
						ac.put(ContextKey.TRIPLE_KEY.toString(), ((TriplePattern) axiom).asTriple());
					} else if (axiom instanceof Functor){
						ac.put(ContextKey.ATOM_KEY.toString(), (Functor) axiom);
					} else {
						continue;
					}
					// Add meta data to context
					this.axioms[i] = ac;
				}
				
				return this;
			}
			@Override
			public void write(Kryo kryo, Output output) {
				output.writeInt(this.axioms.length);
				for (int i = 0; i < this.axioms.length; i++){
					kryo.writeClassAndObject(output, this.axioms[i]);
				}
			}
			@Override
			public void read(Kryo kryo, Input input) {
				this.axioms = new Context[input.readInt()];
				for (int i = 0; i < this.axioms.length; i++){
					this.axioms[i] = (Context) kryo.readClassAndObject(input);
				}
			}
			@Override
			public boolean isStateless() {
				return false;
			}
			@Override
			public boolean forcedUnique() {
				return true;
			}
			
		}.setAxiomList(this.axioms);
	}

	/**
	 * @return the bindings aggregations
	 */
	public List<IVFunction<List<Context>, Context>> getAggregations() {
		return this.aggregations;
	}
	
}