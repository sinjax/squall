package org.openimaj.squall.functions.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.BindingsUtils;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class AtomFilterFunction extends BaseFilterFunction {

	private final static Logger logger = Logger.getLogger(AtomFilterFunction.class);
	
	/**
	 * @param clause
	 * @return
	 */
	public static RuleWrappedAtomFilter ruleWrapped(Functor clause){
		return new RuleWrappedAtomFilter(clause);
	}
	
	private Functor clause;
	
	/**
	 * @param clause construct using a {@link TriplePattern}
	 */
	public AtomFilterFunction(Functor clause) {
		this.clause = clause;
	}

	@Override
	public List<Context> apply(Context inc) {
		List<Context> ctxs = new ArrayList<Context>();
		logger.debug(String.format("Context(%s) sent to Filter(%s)" , inc, this.clause));
		Functor in = inc.getTyped(ContextKey.ATOM_KEY.toString());
		
		Map<String,Node> binds = BindingsUtils.extractVars(this.clause, in);
		if (binds == null) return ctxs;
		
		logger.debug(String.format("Match at Filter(%s): %s", this.clause, inc));
		
		// We have a match!
		Context out = new Context();
		out.put(ContextKey.BINDINGS_KEY.toString(), binds);
		ctxs.add(out);
		return ctxs;
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private AtomFilterFunction(){}
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.clause);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		this.clause = (Functor) kryo.readClassAndObject(input);
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <T>
	 */
	public static class RuleWrappedAtomFilter extends RuleWrappedFunction<AtomFilterFunction> {
		
		protected RuleWrappedAtomFilter(Functor clause){
			super(new AtomFiltARVH(clause));
			this.wrap(new AtomFilterFunction(((AtomFiltARVH) super.getVariableHolder()).clause));
		}
		
		protected static class AtomFiltARVH extends ARVHComponent {

			private final Functor clause;
			
			private AtomFiltARVH(Functor clause){
				Count count = new Count();
				Node[] newArgs = new Node[clause.getArgLength()];
				for (int i = 0; i < clause.getArgs().length; i++){
					newArgs[i] = registerVariable(clause.getArgs()[i], count);
				}
				this.clause = new Functor(clause.getName(), newArgs);
			}
			
			@Override
			public String identifier() {
				StringBuilder obj = new StringBuilder(this.clause.getName())
										.append("(")
										.append(super.stringifyNode(this.clause.getArgs()[0]));
				for (int i = 1; i < this.clause.getArgLength(); i++){
					obj.append(",").append(super.stringifyNode(this.clause.getArgs()[i]));
				}
				return obj.append(")").toString();
			}
			
			@Override
			public String identifier(Map<String, String> varmap) {
				StringBuilder obj = new StringBuilder(this.clause.getName())
										.append("(")
										.append(super.mapNode(varmap, this.clause.getArgs()[0]));
				for (int i = 1; i < this.clause.getArgLength(); i++){
					obj.append(",").append(super.mapNode(varmap, this.clause.getArgs()[i]));
				}
				return obj.append(")").toString();
			}
			
			@Override
			public String toString() {
				return String.format("FILTER: %s, variables: %s",this.clause,this.variables().toString());
			}
			
		}
		
	}

}
