package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.squall.functions.consequences.AtomConsequence;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FunctorConsequence extends AtomConsequence {

	/**
	 * @param r
	 * @param clause
	 * @return
	 */
	public static RuleWrappedFunctorConsequence ruleWrapped(Rule r, Functor clause){
		return new RuleWrappedFunctorConsequence(r, clause);
	}
	
	private Rule rule;
	private Node_RuleVariable[] ruleVariables;
	
	/**
	 * @param r
	 * @param clause
	 */
	public FunctorConsequence(Rule r, Functor clause) {
		super(clause, r.getName());
		this.rule = r;
		if (r != null){
			this.ruleVariables = BindingsUtils.extractRuleVariables(r);
		}
	}
	
	@Override
	public FunctorConsequence clone() throws CloneNotSupportedException {
		FunctorConsequence clone = (FunctorConsequence) super.clone();
		clone.rule = clone.rule.cloneRule();
		clone.ruleVariables = clone.ruleVariables.clone();
		return clone;
	}
	
	protected Map<String, Node> bToMap(BindingVector be) {
		return BindingsUtils.bindingsToMap(be, ruleVariables);
	}

	protected BindingVector mapToB(Map<String, Node> in) { 
		return BindingsUtils.mapToBindings(in, ruleVariables);
	}
	
	@Override
	public List<Context> apply(Context in) {
		Builtin imp = this.getClause().getImplementor();
		Map<String, Node> typed = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		BindingVector be = mapToB(typed);
		final ArrayList<Triple> toret = new ArrayList<Triple>();
		RuleContext context = new ReteTopologyRuleContext(rule, be){
			@Override
			public void add(Triple t) {
				toret.add(t);
			}
		};
		this.getClause().evalAsBodyClause(context);
		
		try{
			if (imp != null) {
				imp.headAction(this.getClause().getBoundArgs(be), this.getClause().getArgLength(), context);
			} else {
				
			}
		} finally{
			
		}
		List<Context> ctxs = new ArrayList<Context>();
		Context out = new Context();
		out.put(ContextKey.TRIPLE_KEY.toString(), toret);
		ctxs.add(out);
		return ctxs;
	}
	private FunctorConsequence(){
		super(new Functor("No Builtin", new Node[0]),"No Rule Name");
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.rule);
		kryo.writeClassAndObject(output, this.ruleVariables);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.rule = (Rule) kryo.readClassAndObject(input);
		this.ruleVariables = (Node_RuleVariable[]) kryo.readClassAndObject(input);
	}

	@Override
	public boolean isReentrant() {
		return true;
	}
	
	protected static class RuleWrappedFunctorConsequence extends RuleWrappedRIFAtomConsequence {

		protected RuleWrappedFunctorConsequence(Rule r, Functor clause) {
			super(new AtomConsARVH(clause, r.getName()));
			super.wrap(new FunctorConsequence(r, clause));
		}
		
	}

}
