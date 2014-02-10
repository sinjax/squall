package org.openimaj.squall.compile.rif.providers.consequences;

import java.util.ArrayList;
import java.util.List;
import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.rules.RIFForAll;
import org.openimaj.squall.functions.consequences.BaseBindingConsequence;
import org.openimaj.squall.functions.consequences.BaseBindingConsequence.RuleWrappedBaseBindingConsequence;
import org.openimaj.squall.functions.consequences.BaseConsequenceFunction.RuleWrappedConsequenceFunction;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFForAllBindingConsequenceProvider extends BaseConsequenceProvider<RIFForAll>{
	
	/**
	 * @param ruleID
	 */
	public RIFForAllBindingConsequenceProvider(String ruleID){
		super(ruleID);
	}

	@Override
	public RuleWrappedConsequenceFunction<BaseBindingConsequence> apply(RIFForAll in) {
		List<Node_Variable> vars = new ArrayList<Node_Variable>();
		for (RIFVar var : in.universalVars()) vars.add(var.getNode());
		return BaseBindingConsequence.ruleWrapped(vars, super.getRuleID());
	}

}
