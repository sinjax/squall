package org.openimaj.squall.compile.rif.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rif.conditions.formula.RIFFormula;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class BindingsUtils {
	
	/**
	 * @param map
	 * @param ruleVars
	 * @return
	 * 		an array of the Jena Nodes that are the bindings of the desired rule variables, in the order defined by the ruleVars array.
	 */
	public static Node[] mapToArray(Map<String,Node> map, String[] ruleVars){
		Node[] vals = new Node[ruleVars.length];
		for (int i = 0; i < ruleVars.length; i++)
			vals[i] = map.get(ruleVars[i]);
		return vals;
	}
	
	/**
	 * @param array
	 * @param ruleVars
	 * @return
	 * 		A Map<String,Node> of Strings to Jena Nodes representing the values of the ruleVariables bound to their variable names.
	 */
	public static Map<String,Node> arrayToMap(Node[] array, String[] ruleVars){
		Map<String,Node> binding = new HashMap<String,Node>();
		for (int i = 0; i < ruleVars.length; i++)
			binding.put(ruleVars[i], array[i]);
		return binding;
	}

	/**
	 * @param formula
	 * @return
	 */
	public static List<Node_RuleVariable> extractRuleVariables(RIFFormula formula) {
		List<Node_RuleVariable> vars = new ArrayList<Node_RuleVariable>();
		extractRuleVariables(formula, vars);
		return vars;
	} 
	
	private static void extractRuleVariables(RIFFormula formula, List<Node_RuleVariable> vars) {
		//TODO
	}  
	
}
