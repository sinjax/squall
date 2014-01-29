package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IConsequence;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FunctorConsequence extends AbstractFunctorFunction<Context,Context> implements IConsequence {

	private Rule rule;
	
	/**
	 * @param r
	 * @param clause
	 */
	public FunctorConsequence(Rule r, Functor clause) {
		super(r, clause);
		this.rule = r;
	}
	
	@Override
	public List<Context> apply(Context in) {
		Builtin imp = this.clause.getImplementor();
		Map<String, Node> typed = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		BindingVector be = mapToB(typed);
		final ArrayList<Triple> toret = new ArrayList<Triple>();
		RuleContext context = new ReteTopologyRuleContext(rule, be){
			@Override
			public void add(Triple t) {
				toret.add(t);
			}
		};
		clause.evalAsBodyClause(context);
		
		try{
			if (imp != null) {
				imp.headAction(clause.getBoundArgs(be), clause.getArgLength(), context);
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
	
	// required for deserialisation by reflection
	private FunctorConsequence(){
		super(null, null);
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.rule);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.rule = (Rule) kryo.readClassAndObject(input);
	}

	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {}

	@Override
	public boolean isReentrant() {
		return true;
	}

}
