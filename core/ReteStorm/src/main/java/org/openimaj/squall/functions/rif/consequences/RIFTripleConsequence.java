package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IConsequence;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFTripleConsequence extends AbstractRIFFunction implements IConsequence {

	private static final Logger logger = Logger.getLogger(RIFTripleConsequence.class);
	private TriplePattern clause;
	private String id;

	/**
	 * @param tp
	 * @param ruleID
	 */
	public RIFTripleConsequence(TriplePattern tp, String ruleID) {
		super();
		Count count = new Count();
		this.clause = new TriplePattern(
			registerVariable(clause.getSubject(), count),
			registerVariable(clause.getPredicate(), count),
			registerVariable(clause.getObject(), count)
		);
		id = ruleID;
	}
	
	private Node getMappedNode(Node node, Map<String, String> varmap){
		if (node.isVariable()){
			return NodeFactory.createVariable(
						varmap.get(
							node.getName()
						)
					);
		} else {
			return node;
		}
	}
	
	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {
		Map<String, String> ruleToBaseVarMap = this.ruleToBaseVarMap();
		Map<String, String> subRuleToBaseVarMap = arvh.ruleToBaseVarMap();
		Map<String, String> thisToARVHVarMap = new HashMap<String, String>();
		for (String rVar : ruleToBaseVarMap().keySet()){
			thisToARVHVarMap.put(ruleToBaseVarMap.get(rVar), subRuleToBaseVarMap.get(rVar));
		}
		
		this.clause = new TriplePattern(
							getMappedNode(this.clause.getSubject(), thisToARVHVarMap),
							getMappedNode(this.clause.getPredicate(), thisToARVHVarMap),
							getMappedNode(this.clause.getObject(), thisToARVHVarMap)
						);
	}
	
	@Override
	protected Node registerVariable(Node n, Count count) {
		n = super.registerVariable(n, count);
		if(Functor.isFunctor(n)){
			Functor f = (Functor)n.getLiteralValue();
			Node[] newArgs = new Node[f.getArgLength()];
			for (int i = 0; i < f.getArgs().length; i++){
				newArgs[i] = super.registerVariable(f.getArgs()[i], count);
			}
			return Functor.makeFunctorNode(f.getName(), newArgs);
		}
		return n;
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		
		List<Triple> ret = new ArrayList<Triple>();
		ret.add(BindingsUtils.instantiate(this.clause,bindings));
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Triple triple : ret) {
			logger.debug(String.format("completed: [%s] -> %s",this.toString(), bindings));
			
			Context out = new Context();
			out.put("triple", triple);
			out.put("rule", this.id);
			ctxs.add(out);			
		}
		return ctxs;
	}


	@Override
	public String identifier() {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return String.format("CONSEQUENCE: clause %s",this.clause.toString());
	}
	
}