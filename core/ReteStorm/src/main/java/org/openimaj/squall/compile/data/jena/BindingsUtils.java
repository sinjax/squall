package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class BindingsUtils {
	// A mapping between index and a node rule variable
	/**
	 * @param r
	 * @return the variables in this rule's head and body
	 */
	public static Node_RuleVariable[] extractRuleVariables(Rule r) {
		List<Node_RuleVariable> vars = extractRuleVariables(r.getBody());
		vars.addAll(extractRuleVariables(r.getHead()));
		Node_RuleVariable[] ret = new Node_RuleVariable[r.getNumVars()];
		for (Node_RuleVariable node_RuleVariable : vars) {
			ret[node_RuleVariable.getIndex()] = node_RuleVariable;
		}
		return ret;
	}

	/**
	 * @param ce
	 * @return the variables in all the {@link ClauseEntry} provided
	 */
	public static List<Node_RuleVariable> extractRuleVariables(ClauseEntry[] ce) {
		List<Node_RuleVariable> ret = new ArrayList<Node_RuleVariable>();
		for (int i = 0; i < ce.length; i++) {
			ClauseEntry ceEnt = ce[i];
			if (ceEnt instanceof TriplePattern) {
				ret.addAll(extractRuleVariables((TriplePattern) ceEnt));
			} else if (ceEnt instanceof Functor) {
				ret.addAll(extractRuleVariables((Functor) ceEnt));
			}
		}
		return ret;
	}

	/**
	 * @param n
	 * @return the variables in the node
	 */
	public static List<Node_RuleVariable> extractRuleVariables(Node n) {

		List<Node_RuleVariable> ret = new ArrayList<Node_RuleVariable>();
		if (n instanceof Node_RuleVariable) {
			ret.add((Node_RuleVariable) n);
		} else if (Functor.isFunctor(n)) {
			Functor f = (Functor) n.getLiteralValue();
			ret.addAll(extractRuleVariables(f));
		}
		return ret;
	}

	/**
	 * @param ceEnt
	 * @return the variables in the {@link Functor}
	 */
	public static List<Node_RuleVariable> extractRuleVariables(Functor ceEnt) {
		List<Node_RuleVariable> ret = new ArrayList<Node_RuleVariable>();
		Node[] args = ceEnt.getArgs();
		for (Node node : args) {
			ret.addAll(extractRuleVariables(node));
		}
		return ret;
	}

	/**
	 * @param ceEnt
	 * @return the variables in the {@link TriplePattern}
	 */
	public static List<Node_RuleVariable> extractRuleVariables(
			TriplePattern ceEnt) {
		List<Node_RuleVariable> ret = new ArrayList<Node_RuleVariable>();
		ret.addAll(extractRuleVariables(ceEnt.getSubject()));
		ret.addAll(extractRuleVariables(ceEnt.getPredicate()));
		ret.addAll(extractRuleVariables(ceEnt.getObject()));
		return ret;
	}
	
	/**
	 * @param be
	 * @param ruleVariables
	 * @return A {@link BindingVector} to a {@link Map}. The variable names are defined by the {@link Node_RuleVariable} list
	 */
	public static Map<String, Node> bindingsToMap(BindingVector be, Node_RuleVariable[] ruleVariables) {
		
		Map<String, Node> ret = new HashMap<String, Node>();
		Node[] env = be.getEnvironment();
		for (int i = 0; i < env.length; i++) {
			ret.put(ruleVariables[i].getName(), env[i]);
		}
		return ret;
	}

	/**
	 * @param in
	 * @param ruleVariables
	 * @return A {@link BindingVector} ordered by the {@link Node_RuleVariable} list with values from the {@link Map} or null otherwise
	 */
	public static BindingVector mapToBindings(Map<String, Node> in, Node_RuleVariable[] ruleVariables) { 
		
		Node[] bindings = new Node[ruleVariables.length];
		for (Node_RuleVariable node : ruleVariables) {
			Node set = in.get(node.getName());
			if(set!=null){
				bindings[node.getIndex()] = set;
			}
		}
		BindingVector be = new BindingVector(bindings);
		return be ;
	}

}
