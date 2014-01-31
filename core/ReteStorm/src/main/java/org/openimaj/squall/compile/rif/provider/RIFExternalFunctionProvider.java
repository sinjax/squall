package org.openimaj.squall.compile.rif.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFDatum;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.InheritsVariables;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.squall.functions.rif.calculators.BaseRIFValueFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A function which given a {@link RIFExternal} can provide a working implementation
 * of that function.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class RIFExternalFunctionProvider extends FunctionProvider<RIFExternal,RIFExpr> {
	
	/**
	 * @param reg
	 */
	public RIFExternalFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	@Override
	public RuleWrappedFunction<? extends BaseRIFPredicateFunction> apply(RIFExternal in) {
		if(in instanceof RIFExternalExpr) return apply((RIFExternalExpr)in);
		else return apply((RIFExternalValue)in);
	}
	/**
	 * @param in
	 * @return
	 */
	public abstract RuleWrappedFunction<? extends BaseRIFPredicateFunction> apply(RIFExternalExpr in) ;
	
	/**
	 * @param in
	 * @return
	 */
	public abstract RuleWrappedFunction<? extends BaseRIFPredicateFunction> apply(RIFExternalValue in) ;
	
	protected IndependentPair<Node[],Map<Node, RuleWrappedValueFunction<?>>> extractNodesAndSubFunctions(RIFAtom atom) {
		List<Node> nodes = new ArrayList<Node>();
		Map<Node, RuleWrappedValueFunction<?>> funcMap = new HashMap<Node, RuleWrappedValueFunction<?>>();
		for (RIFDatum node : atom) {
			nodes.add(node.getNode());
			if (node instanceof RIFExpr){
				RuleWrappedValueFunction<?> varValFunc;
				if (node instanceof RIFExternal){
					RIFExternal exp = (RIFExternal) node;
					varValFunc = (RuleWrappedValueFunction<?>) RIFExternalFunctionRegistry.compile(exp);
				} else {
					RIFExpr exp = (RIFExpr) node;
					varValFunc = (RuleWrappedValueFunction<?>) compileFromRegistry(exp);
				}
				funcMap.put(node.getNode(), varValFunc);
			}
		}
		
		Node[] nodeArr = (Node[]) nodes.toArray(new Node[0]);
		
		return new IndependentPair<Node[], Map<Node,RuleWrappedValueFunction<?>>>(nodeArr,funcMap);
	}
	
	protected static abstract class RuleWrappedPredicateFunction<T extends BaseRIFPredicateFunction>
									extends RuleWrappedFunction<T>
									implements InheritsVariables {
		
		private AnonimisedRuleVariableHolder sourceVarHolder;
		private final String functionName;
		private final Node[] nodes;
		private final Map<Node, RuleWrappedValueFunction<?>> funcMap;
		
		public RuleWrappedPredicateFunction(String fn, Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap){
			super();
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
		
		protected Map<Node, BaseRIFValueFunction> getRulelessFuncMap(){
			Map<Node,BaseRIFValueFunction> rulelessFuncMap = new HashMap<Node, BaseRIFValueFunction>();
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
		public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh) {
			if (this.areSourceVariablesSet()){
				throw new RuntimeException("Cannot set more than one source variable template for a predicate function.");
			}
			Map<String, String> arvhVarMap = arvh.ruleToBaseVarMap();
			Map<String, String> thisVarMap = this.ruleToBaseVarMap();
			
			Map<String, String> directVarMap = new HashMap<String, String>();
			for (String ruleVar : thisVarMap.keySet()){
				if (arvhVarMap.containsKey(ruleVar)){
					directVarMap.put(thisVarMap.get(ruleVar), arvhVarMap.get(ruleVar));
				} else {
					return false;
				}
			}
			
			super.getWrapped().mapNodeVarNames(directVarMap);
			
			if (super.resetVars()){
				for (String ruleVar : arvh.ruleVariables()){
					String baseVar = arvhVarMap.get(ruleVar);
					super.addVariable(baseVar);
					super.putRuleToBaseVarMapEntry(ruleVar, baseVar);
				}
			}
			
			this.sourceVarHolder = arvh;
			return true;
		}
		
		@Override
		public String getSourceVarHolderIdent() {
			return this.areSourceVariablesSet() ? this.sourceVarHolder.identifier() : "No Source";
		}
		
		@Override
		public String getSourceVarHolderIdent(Map<String, String> varMap) {
			return this.areSourceVariablesSet() ? this.sourceVarHolder.identifier(varMap) : "No Source";
		}
		
	}
	
	protected static abstract class RuleWrappedValueFunction<T extends BaseRIFValueFunction>
									extends RuleWrappedPredicateFunction<T> {
		
		private String resultName; 
		
		public RuleWrappedValueFunction(String fn, Node[] ns, Node_Variable nr, Map<Node, RuleWrappedValueFunction<?>> funcMap){
			super(fn, ns, funcMap);
			this.resultName = nr.getName();
			super.addVariable(this.resultName);
			super.putRuleToBaseVarMapEntry(this.resultName, this.resultName);
		}
	
		@Override
		public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh) {
			BaseRIFValueFunction valFunc = (BaseRIFValueFunction) super.getWrapped();
			if (super.setSourceVariables(arvh)){
				String newBaseResultName = valFunc.getResultVarNode().getName();
				super.addVariable(this.resultName);
				super.putRuleToBaseVarMapEntry(this.resultName, newBaseResultName);
				
				return true;
			}
			return false;
			
		}
		
	}

}
