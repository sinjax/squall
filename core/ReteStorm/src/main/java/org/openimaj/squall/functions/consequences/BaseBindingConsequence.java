package org.openimaj.squall.functions.consequences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.BindingsUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseBindingConsequence extends BaseConsequenceFunction {

	private static final Logger logger = Logger.getLogger(BaseBindingConsequence.class);
	
	/**
	 * @param vars
	 * @param rid
	 * @return
	 */
	public static RuleWrappedBaseBindingConsequence ruleWrapped(List<Node_Variable> vars, String rid){
		return new RuleWrappedBaseBindingConsequence(vars, rid);
	}
	
	private String[] inVars;
	private String[] outVars;
	
	/**
	 * @param vars 
	 * @param ruleID 
	 */
	public BaseBindingConsequence(List<Node_Variable> vars, String ruleID){
		super(ruleID);
		this.outVars = new String[vars.size()];
		for (int i = 0; i < this.outVars.length; i++){
			this.outVars[i] = vars.get(i).getName();
		}
	}
	
	@Override
	protected BaseBindingConsequence clone() throws CloneNotSupportedException {
		BaseBindingConsequence bbc = (BaseBindingConsequence) super.clone();
		if (bbc.inVars != null){
			bbc.inVars = bbc.inVars.clone();
		}
		bbc.outVars = bbc.outVars.clone();
		return bbc;
	}
	
	@Override
	public void mapVarNames(Map<String, String> varMap) {
		this.inVars = new String[this.outVars.length];
		
		for (int i = 0; i < this.inVars.length; i++){
			this.inVars[i] = varMap.get(this.outVars[i]);
		}
	}
	
	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		Map<String,Node> ret = BindingsUtils.arrayToMap(
									BindingsUtils.mapToArray(
										bindings,
										this.inVars
									),
									this.outVars
								);
		
		Context out = new Context();
		logger.debug(this.toString());
		out.put(ContextKey.BINDINGS_KEY.toString(), ret);
		out.put("rule", super.getRuleID());
		List<Context> ctxs = new ArrayList<Context>();
		ctxs.add(out);
		return ctxs;
	}
	
	@Override
	public String toString() {
		return String.format("CONSEQUENCE: inVariables %s -> outVariables %s", Arrays.toString(this.inVars), Arrays.toString(this.outVars));
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private BaseBindingConsequence(){
		super("No Rule ID");
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.inVars);
		kryo.writeClassAndObject(output, this.outVars);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.inVars = (String[]) kryo.readClassAndObject(input);
		this.outVars = (String[]) kryo.readClassAndObject(input);
	}

	@Override
	public boolean isReentrant() {
		return false;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedBaseBindingConsequence extends RuleWrappedConsequenceFunction<BaseBindingConsequence> {

		protected RuleWrappedBaseBindingConsequence(List<Node_Variable> vars, String rid) {
			super(new BindingConsARVH(vars, rid));
			this.wrap(new BaseBindingConsequence(vars, rid));
		}
		
		protected static class BindingConsARVH extends ConsequenceARVH {
			
			protected BindingConsARVH(List<Node_Variable> vars, String rID) {
				super(rID);
				Count count = new Count();
				for (Node_Variable var : vars){
					super.registerVariable(var, count);
				}
			}

			@Override
			public String identifier(Map<String, String> varmap) {
				StringBuilder ident = super.getRuleBody();
				
				Map<String, String> r2BVarMap = super.ruleToBaseVarMap();
				int i = 0;
				ident.append(r2BVarMap.get(super.getVariable(i)))
					 .append(" as ")
					 .append(varmap.get(super.getVariable(i)));
				for (i++; i < super.varCount(); i++){
					ident.append(", ")
						 .append(r2BVarMap.get(super.getVariable(i)))
						 .append(" as ")
						 .append(varmap.get(super.getVariable(i)));
				}
				
				return ident.toString();
			}

			@Override
			public String identifier() {
				StringBuilder ident = super.getRuleBody();
				
				Map<String, String> r2BVarMap = super.ruleToBaseVarMap();
				int i = 0;
				ident.append(r2BVarMap.get(super.getVariable(i)))
					 .append(" as ")
					 .append(super.getVariable(i));
				for (i++; i < super.varCount(); i++){
					ident.append(", ")
						 .append(r2BVarMap.get(super.getVariable(i)))
						 .append(" as ")
						 .append(super.getVariable(i));
				}
				
				return ident.toString();
			}
		}
		
	}

}
