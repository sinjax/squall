package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.util.data.Context;

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
public class FunctorConsequence extends AbstractFunctorFunction<Context,Context> {

	/**
	 * @param r
	 * @param clause
	 */
	public FunctorConsequence(Rule r, Functor clause) {
		super(r, clause);
	}
	
	@Override
	public Context apply(Context in) {
		Builtin imp = this.clause.getImplementor();
		Map<String, Node> typed = in.getTyped("bindings");
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
		Context out = new Context();
		out.put("triples", toret);
		return out;
	}

}
