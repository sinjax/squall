package org.openimaj.squall.compile.data.rif;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;

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
	 * @param clause
	 * @param bindings
	 * @return A triple instansiation of the {@link TriplePattern} with its variables filled in from the bindings map
	 */
	public static Triple instantiate(TriplePattern clause,Map<String, Node> bindings) {
		
		Node s = extractVariable(bindings, clause.getSubject());
		Node p = extractVariable(bindings, clause.getPredicate());
		Node o = extractVariable(bindings, clause.getObject());
		
		return new Triple(s, p, o);
	}

	private static Node extractVariable(Map<String, Node> bindings, Node subject) {
		Node s;
		if(subject.isVariable()){
			s = bindings.get(subject.getName());
			if(s == null){
				s = NodeFactory.createAnon();
			}
		}
		else{
			s = subject;
		}
		return s;
	}  
	
}
