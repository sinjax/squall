package org.openimaj.squall.functions.rif.predicates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;
import org.openimaj.squall.functions.rif.calculators.BaseRIFValueFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

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
public abstract class BaseRIFPredicateFunction extends AbstractRIFFunction {
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
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
	
	protected Node[] nodes;
	protected Map<Node,BaseRIFValueFunction> funcs;
	
	/**
	 * Constructs a new predicate function that filters bindings predicated on some function of the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @param funcs 
	 * @throws RIFPredicateException 
	 */
	public BaseRIFPredicateFunction(Node[] ns, Map<Node, BaseRIFValueFunction> funcs) throws RIFPredicateException {
		super();
		this.nodes = ns;
		this.funcs = funcs;
	}
	
	/**
	 * @param directVarMap
	 * @return 
	 */
	public int mapNodeVarNames(Map<String, String> directVarMap) {
		for (int i = 0; i < nodes.length; i++){
			if (nodes[i].isVariable()){
				if (directVarMap.containsKey(nodes[i].getName())) {
					nodes[i] = NodeFactory.createVariable(
									directVarMap.get(
										nodes[i].getName()
									)
								);
				} else if (this.funcs.containsKey(nodes[i])){
					// store the function in a variable then map its vars.
					BaseRIFValueFunction valFunc = this.funcs.get(nodes[i]);
					valFunc.mapNodeVarNames(directVarMap);
					// remove the function from the map of functions
					this.funcs.remove(nodes[i]);
					// replace the original variable node in the root function with the mapped variable node
					nodes[i] = valFunc.getResultVarNode();
					// put the function back into the map of functions with the mapped variable node as the key
					this.funcs.put(nodes[i], valFunc);
				}
			}
		}
		return directVarMap.size();
	}
	
	protected Object extractBinding(Map<String, Node> binds, Node node) {
		if(node.isVariable()){
			Node boundNode = binds.get(node.getName());
			if(boundNode == null){
				throw new UnsupportedOperationException("Unbound variable");
			}
			node = boundNode;
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
	public List<Context> apply(Context in) {
		Map<String, Node> oldBinds = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		Map<String, Node> newBinds = new HashMap<String, Node>();
		newBinds.putAll(oldBinds);
		
		for (Node n : this.funcs.keySet()){
			BaseRIFValueFunction valFunc = this.funcs.get(n);
			List<Context> processedList = valFunc.apply(in);
			Context processed = processedList.get(0);
			Map<String, Node> processedBinds = processed.getTyped(ContextKey.BINDINGS_KEY.toString());
			newBinds.put(n.getName(), processedBinds.get(valFunc.getResultVarNode()));
		}
		
		Context populatedIn = new Context();
		populatedIn.putAll(in);
		populatedIn.put(ContextKey.BINDINGS_KEY.toString(), newBinds);
		
		return applyRoot(populatedIn);
	}
	
	protected abstract List<Context> applyRoot(Context in);
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.nodes);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		this.nodes = (Node[]) kryo.readClassAndObject(input);
	}
	
	@Override
	public boolean isStateless() {
		return true;
	}
	
	@Override
	public boolean forcedUnique() {
		return false;
	}
	
}
