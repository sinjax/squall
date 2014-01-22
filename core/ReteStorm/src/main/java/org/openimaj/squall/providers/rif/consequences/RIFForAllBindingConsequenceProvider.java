package org.openimaj.squall.providers.rif.consequences;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.rules.RIFForAll;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.functions.rif.consequences.BaseBindingConsequence;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFForAllBindingConsequenceProvider implements Function<RIFForAll, IVFunction<Context, Context>>{
	
	private String rID;
	
	public RIFForAllBindingConsequenceProvider(String ruleID){
		this.rID = ruleID;
	}

	@Override
	public IVFunction<Context, Context> apply(RIFForAll in) {
		List<Node_Variable> vars = new ArrayList<Node_Variable>();
		for (RIFVar var : in.universalVars()) vars.add(var.getNode());
		return new BaseBindingConsequence(vars, this.rID);
	}

}
