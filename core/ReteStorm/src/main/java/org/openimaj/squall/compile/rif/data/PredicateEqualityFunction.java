package org.openimaj.squall.compile.rif.data;

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
public class PredicateEqualityFunction implements IVFunction<Context, Context> {

	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	@SuppressWarnings("serial")
	public static class PredicateEqualityException extends Exception {

		/**
		 * @param message
		 */
		public PredicateEqualityException(String message) {
			super(message);
		}
		
		/**
		 * @param e
		 */
		public PredicateEqualityException(Exception e) {
			super(e);
		}
		
		/**
		 * @param message
		 * @param e
		 */
		public PredicateEqualityException(String message, Exception e) {
			super(message,e);
		}
		
	}
	
	private String[] vars;
	private String[] anonVars;
	private Map<String,Integer> varMap;
	private Node val;
	private String anonimisedName;
	
	/**
	 * Constructs a new equality-checking predicate function that filters bindings predicated on equality between  
	 * @param ns 
	 * @throws PredicateEqualityException 
	 */
	public PredicateEqualityFunction(Node[] ns) throws PredicateEqualityException{
		int count = 0;
		this.val = null;
		for (Node n : ns){
			if (n.isVariable())
				count++;
			else if (this.val == null)
				this.val = n;
			else if (!this.val.sameValueAs(n))
				throw new PredicateEqualityException("RIF translator: All constants compared must be semantically equal.");
		}
		if (count == 0){
			throw new PredicateEqualityException("RIF translator: Must compare some variables.");
		}
		this.vars = new String[count];
		this.anonVars = new String[count];
		for (Node n : ns){
			if (n.isVariable()){
				this.vars[--count] = ((Node_RuleVariable) n).getName();
				this.anonVars[--count] = ""+((Node_RuleVariable) n).getIndex();
			}
		}
		
		this.varMap = new HashMap<String,Integer>();
		for (int i = 0; i < this.vars.length; i++)
			this.varMap.put(this.vars[i], i);
		this.anonimisedName = this.anonimised(this.varMap);
	}
	
	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		Node[] env = BindingsUtils.mapToArray(bindings,vars);
		
		try{
			if (!val.sameValueAs(env[0])) return null;
		} catch (NullPointerException e) {}
		
		for (int j = 1; j < env.length; j++){
			if (!env[0].sameValueAs(env[j])) return null;
		}
		
		List<Context> ret = new ArrayList<Context>();
		ret.add(in);
		return ret;
	}

	@Override
	public List<String> variables() {
		return Arrays.asList(this.vars);
	}
	
	public void mapVariables(Map<String,String> map){
		String[] newVars = new String[this.vars.length];
		for (int i = 0; i < newVars.length; i++)
			newVars[i] = map.get(this.vars[i]);
		this.vars = newVars;
	}

	@Override
	public String anonimised(Map<String, Integer> varMap) {
		StringBuilder anon = new StringBuilder();
		anon.append("?");
		anon.append(varMap.get(this.vars[0]));
		for (int i = 1; i < this.vars.length; i++){
			anon.append(" = ?");
			anon.append(varMap.get(this.vars[i]));
		}
		return anon.toString();
	}

	@Override
	public String anonimised() {
		return this.anonimisedName;
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
