package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public abstract class BaseRIFPredicateFunction implements IVFunction<Context, Context> {

	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	@SuppressWarnings("serial")
	public static class RIFPredicateException extends Exception {

		/**
		 * @param message
		 */
		public RIFPredicateException(String message) {
			super(message);
		}
		
		/**
		 * @param e
		 */
		public RIFPredicateException(Exception e) {
			super(e);
		}
		
		/**
		 * @param message
		 * @param e
		 */
		public RIFPredicateException(String message, Exception e) {
			super(message,e);
		}
		
	}
	
	protected String[] vars;
	protected String anonimisedName;
	
	@Override
	public List<String> variables() {
		return Arrays.asList(this.vars);
	}
	
	@Override
	public void mapVariables(Map<String,String> map){
		String[] newVars = new String[this.vars.length];
		for (int i = 0; i < newVars.length; i++)
			newVars[i] = map.get(this.vars[i]);
		this.vars = newVars;
	}
	
	@Override
	public String anonimised() {
		return this.anonimisedName;
	}
	
	/**
	 * Constructs a new predicate function that filters bindings predicated on some function of the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @throws RIFPredicateException 
	 */
	public BaseRIFPredicateFunction(Node[] ns) throws RIFPredicateException {
		List<String> variables = new ArrayList<String>();
		for (Node n : ns){
			if (n.isVariable()){
				variables.add(((Node_RuleVariable) n).getName());
			}
		}
		if (variables.size() < 1){
			throw new RIFPredicateException("RIF translator: Must compare some variable(s).");
		}
		this.vars = new String[0];
		this.vars = variables.toArray(this.vars);
	}
	
	@Override
	public String anonimised(Map<String, Integer> varmap) {
		return null;
	}

	@Override
	public void setup() {}

	@Override
	public void cleanup() {}
	
}
