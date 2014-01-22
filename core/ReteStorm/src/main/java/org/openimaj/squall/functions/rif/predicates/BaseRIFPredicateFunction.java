package org.openimaj.squall.functions.rif.predicates;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IPredicate;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public abstract class BaseRIFPredicateFunction extends AbstractRIFFunction implements IPredicate {

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
	
	protected AnonimisedRuleVariableHolder varHolder;
	protected Node[] nodes;
	
	/**
	 * Constructs a new predicate function that filters bindings predicated on some function of the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @throws RIFPredicateException 
	 */
	public BaseRIFPredicateFunction(Node[] ns) throws RIFPredicateException {
		super();
		this.varHolder = null;
		
		Count count = new Count();
		for (int i = 0; i < ns.length; i++){
			if (registerVariable(ns[i], count) == Node.ANY){
				ns[i] = NodeFactory.createVariable(Integer.toString(count.getCount()));
			}
		}
		if (this.varCount() < 1){
			throw new RIFPredicateException("RIF translator: Must compare some variable(s).");
		}
		
		this.nodes = ns;
	}
	
	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {
		this.varHolder = arvh;
		Map<String, String> thisVarMap = this.ruleToBaseVarMap();
		Map<String, String> arvhVarMap = this.varHolder.ruleToBaseVarMap();
		
		Map<String, String> directVarMap = new HashMap<String, String>();
		for (int i = 0; i < this.varCount(); i++){
			directVarMap.put(thisVarMap.get(this.getVariable(i)), arvhVarMap.get(this.getVariable(i)));
		}
		
		for (int i = 0; i < nodes.length; i++){
			if (nodes[i].isVariable()) {
				nodes[i] = NodeFactory.createVariable(
								directVarMap.get(
									nodes[i].getName()
								)
							);
			}
		}
	}
	
	@Override
	public String[] variables() {
		if (this.varHolder != null){
			return this.varHolder.variables();
		}
		return super.variables();
	}
	
	@Override
	public String getVariable(int index) {
		return this.varHolder == null ? super.getVariable(index) : this.varHolder.getVariable(index);
	}
	
	protected Object extractBinding(Map<String, Node> binds, Node node) {
		if(node.isVariable()){
			node = binds.get(node.getName());
			if(node == null){
				throw new UnsupportedOperationException("Unbound variable");
			}
		}
		if(node.isConcrete()){
			Node_Concrete lit = (Node_Concrete) node;
			if (lit.isLiteral())
				return lit.getLiteralValue();
			else if (lit.isURI())
				return lit.getURI();
			else if (lit.isBlank())
				return lit.getBlankNodeLabel();
		}
		throw new UnsupportedOperationException("Incorrect node type for comparison: " + node);
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.nodes);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		this.nodes = (Node[]) kryo.readClassAndObject(input);
	}
	
}
