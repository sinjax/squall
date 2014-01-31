package org.openimaj.squall.providers.rif.consequences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.rules.RIFForAll;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.compile.rif.provider.FunctionProvider;
import org.openimaj.squall.compile.rif.provider.FunctionRegistry;
import org.openimaj.squall.compile.rif.provider.RIFExprFunctionRegistry;
import org.openimaj.squall.functions.rif.consequences.BaseBindingConsequence;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFForAllBindingConsequenceProvider extends BaseRIFConsequenceProvider<RIFForAll>{
	
	private String rID;
	
	public RIFForAllBindingConsequenceProvider(String ruleID, RIFExprFunctionRegistry reg){
		super(reg);
		this.rID = ruleID;
	}

	@Override
	public RuleWrappedBaseBindingConsequence apply(RIFForAll in) {
		List<Node_Variable> vars = new ArrayList<Node_Variable>();
		for (RIFVar var : in.universalVars()) vars.add(var.getNode());
		return new BaseBindingConsequence(vars, this.rID);
	}
	
	private static class RuleWrappedBaseBindingConsequence extends RuleWrappedConsequenceFunction<BaseBindingConsequence> {

		public RuleWrappedBaseBindingConsequence() {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public String identifier(Map<String, String> varmap) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String identifier() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
