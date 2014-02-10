package org.openimaj.squall.functions.predicates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.BaseContextIFunction;
import org.openimaj.squall.compile.data.InheritsVariables;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
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
public abstract class BasePredicateFunction extends BaseContextIFunction {
	
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
	
	private Node[] nodes;
	private Map<Node,BaseValueFunction> funcs;
	
	/**
	 * Constructs a new predicate function that filters bindings predicated on some function of the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @param funcs 
	 * @throws RIFPredicateException 
	 */
	public BasePredicateFunction(Node[] ns, Map<Node, BaseValueFunction> funcs) throws RIFPredicateException {
		this.nodes = ns;
		this.funcs = funcs;
	}
	
	protected Node getNode(int index){
		return this.nodes[index];
	}
	
	protected Node[] getNodes(){
		Node[] ret = new Node[this.nodes.length];
		for (int i = 0; i < ret.length; i++){
			ret[i] = this.nodes[i];
		}
		return ret;
	}
	
	protected int getNodeCount(){
		return this.nodes.length;
	}
	
	@Override
	public BasePredicateFunction clone() throws CloneNotSupportedException {
		BasePredicateFunction bpf = (BasePredicateFunction) super.clone();
		bpf.nodes = bpf.nodes.clone();
		Map<Node, BaseValueFunction> map = new HashMap<Node, BaseValueFunction>();
		for (Node key : bpf.funcs.keySet()){
			map.put(key, bpf.funcs.get(key).clone());
		}
		bpf.funcs = map;
		return bpf;
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
					BaseValueFunction valFunc = this.funcs.get(nodes[i]);
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
	
	protected Object extractBinding(Map<String, Node> binds, int nodeIndex) {
		Node node = this.nodes[nodeIndex];
		if(node.isVariable()){
			Node boundNode = binds.get(this.nodes[nodeIndex].getName());
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
			BaseValueFunction valFunc = this.funcs.get(n);
			List<Context> processedList = valFunc.apply(in);
			Context processed = processedList.get(0);
			Map<String, Node> processedBinds = processed.getTyped(ContextKey.BINDINGS_KEY.toString());
			newBinds.put(n.getName(), processedBinds.get(valFunc.getResultVarNode().getName()));
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
		kryo.writeClassAndObject(output, this.funcs);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		this.nodes = (Node[]) kryo.readClassAndObject(input);
		this.funcs = (Map<Node, BaseValueFunction>) kryo.readClassAndObject(input);
	}
	
	@Override
	public boolean isStateless() {
		return true;
	}
	
	@Override
	public boolean forcedUnique() {
		return false;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <T>
	 */
	public static abstract class RuleWrappedPredicateFunction<T extends BasePredicateFunction>
									extends RuleWrappedFunction<T>
									implements InheritsVariables {
		
		private PredFuncARVH varHolder;
		
		/**
		 * @param fn
		 * @param ns
		 * @param funcMap
		 */
		protected RuleWrappedPredicateFunction(String fn, Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap){
			super(new PredFuncARVH(fn, ns, funcMap));
			this.varHolder = (PredFuncARVH) super.getVariableHolder();
		}
		
		@Override
		public RuleWrappedPredicateFunction<T> clone() throws CloneNotSupportedException {
			RuleWrappedPredicateFunction<T> clone = (RuleWrappedPredicateFunction<T>) super.clone();
			clone.varHolder = (PredFuncARVH) clone.getVariableHolder();
			clone.wrap((T) clone.getWrapped().clone());
			return clone;
		}
		
		protected Map<Node, BaseValueFunction> getRulelessFuncMap(){
			return this.varHolder.getRulelessFuncMap();
		}
		
		@Override
		public boolean areSourceVariablesSet() {
			return this.varHolder.areSourceVariablesSet();
		}
		
		@Override
		public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh) {
			if (this.areSourceVariablesSet()){
				throw new RuntimeException("Cannot set more than one source variable template for a predicate function.");
			}
			Map<String, String> arvhVarMap = arvh.ruleToBaseVarMap();
			Map<String, String> thisVarMap = this.ruleToBaseVarMap();
			
			Map<String, String> directVarMap = new HashMap<String, String>();
			for (String ruleVar : arvhVarMap.keySet()){
				if (thisVarMap.containsKey(ruleVar)){
					directVarMap.put(thisVarMap.get(ruleVar), arvhVarMap.get(ruleVar));
				} else {
					directVarMap.put(ruleVar, arvhVarMap.get(ruleVar));
				}
			}
			
			super.getWrapped().mapNodeVarNames(directVarMap);
			
			this.varHolder.setSourceVariables(arvh);
			return true;
		}
		
		@Override
		public String getSourceVarHolderIdent() {
			return this.varHolder.getSourceVarHolderIdent();
		}
		
		@Override
		public String getSourceVarHolderIdent(Map<String, String> varMap) {
			return this.varHolder.getSourceVarHolderIdent(varMap);
		}
		
		protected static class PredFuncARVH extends ARVHComponent implements InheritsVariables {

			private AnonimisedRuleVariableHolder sourceVarHolder;
			private String functionName;
			private Node[] nodes;
			private Map<Node, RuleWrappedValueFunction<?>> funcMap;
			
			protected PredFuncARVH(String fn, Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap){
				this.sourceVarHolder = null;
				this.functionName = fn;
				this.nodes = ns;
				this.funcMap = funcMap;
				for (int i = 0; i < ns.length; i++){
					if (ns[i].isVariable() && !funcMap.containsKey(ns[i])){
						super.addVariable(ns[i].getName());
						super.putRuleToBaseVarMapEntry(ns[i].getName(), ns[i].getName());
					}
				}
				for (Node rn : funcMap.keySet()){
					RuleWrappedValueFunction<?> arvh = funcMap.get(rn);
					Map<String, String> arvhR2BVarMap = arvh.ruleToBaseVarMap();
					for (String rVar : arvh.ruleVariables()){
						String bVar = arvhR2BVarMap.get(rVar);
						super.addVariable(bVar);
						super.putRuleToBaseVarMapEntry(rVar, bVar);
					}
				}
			}
			
			@Override
			public PredFuncARVH clone()
					throws CloneNotSupportedException {
				PredFuncARVH clone = (PredFuncARVH) super.clone();
				clone.nodes = clone.nodes.clone();
				Map<Node, RuleWrappedValueFunction<?>> map = new HashMap<Node, RuleWrappedValueFunction<?>>();
				for (Node key : clone.funcMap.keySet()){
					map.put(key, clone.funcMap.get(key).clone());
				}
				clone.funcMap = map;
				return clone;
			}
			
			protected Map<Node, BaseValueFunction> getRulelessFuncMap(){
				Map<Node,BaseValueFunction> rulelessFuncMap = new HashMap<Node, BaseValueFunction>();
				for (Node n : this.funcMap.keySet()){
					rulelessFuncMap.put(n, this.funcMap.get(n).getWrapped());
				}
				return rulelessFuncMap;
			}
			
			@Override
			protected String stringifyNode(Node node) {
				if (funcMap.containsKey(node)){
					return funcMap.get(node).identifier();
				}
				return super.stringifyNode(node);
			}
			
			@Override
			protected String mapNode(Map<String, String> varmap, Node node) {
				if (funcMap.containsKey(node)){
					return funcMap.get(node).identifier(varmap);
				}
				return super.mapNode(varmap, node);
			}
			
			private String printArgList(){
				if (this.nodes.length > 0){
					int i = 0;
					StringBuilder anon = new StringBuilder(super.stringifyNode(this.nodes[i]));
					for (i++; i < this.nodes.length; i++){
						anon.append(",").append(super.stringifyNode(this.nodes[i]));;
					}
					return anon.toString();
				}
				return "";
			}
			
			private String printArgList(Map<String, String> varMap){
				if (this.nodes.length > 0){
					int i = 0;
					StringBuilder anon = new StringBuilder(this.mapNode(varMap, this.nodes[i]));
					for (i++; i < this.nodes.length; i++){
						anon.append(",").append(this.mapNode(varMap, this.nodes[i]));;
					}
					return anon.toString();
				}
				return "";
			}
			
			@Override
			public String identifier(Map<String, String> varmap) {
				return new StringBuilder(this.getSourceVarHolderIdent(varmap))
				.append(this.functionName)
				.append("(")
				.append(this.printArgList(varmap))
				.append(")")
				.toString();
			}

			@Override
			public String identifier() {
				return new StringBuilder(this.getSourceVarHolderIdent())
							.append(this.functionName)
							.append("(")
							.append(this.printArgList())
							.append(")")
							.toString();
			}
			
			@Override
			public boolean areSourceVariablesSet() {
				return this.sourceVarHolder != null;
			}

			@Override
			public String getSourceVarHolderIdent() {
				return this.areSourceVariablesSet() ? this.sourceVarHolder.identifier() : "No Source";
			}
			
			@Override
			public String getSourceVarHolderIdent(Map<String, String> varMap) {
				return this.areSourceVariablesSet() ? this.sourceVarHolder.identifier(varMap) : "No Source";
			}

			@Override
			public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh) {
				if (super.resetVars()){
					Map<String, String> arvhVarMap = arvh.ruleToBaseVarMap();
					for (String ruleVar : arvh.ruleVariables()){
						String baseVar = arvhVarMap.get(ruleVar);
						super.addVariable(baseVar);
						super.putRuleToBaseVarMapEntry(ruleVar, baseVar);
					}
				}
				
				this.sourceVarHolder = arvh;
				return true;
			}
			
		}
		
	}

	protected Map<Node, BaseValueFunction> getFuncMap() {
		return this.funcs;
	}
	
}
