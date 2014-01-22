package org.openimaj.squall.functions.rif.core;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.rules.RIFForAll;
import org.openimaj.squall.functions.rif.consequences.BaseBindingConsequence;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFForAllBindingConsequence extends BaseBindingConsequence {

	private static List<Node_Variable> extractBindings(RIFForAll fa){
		List<Node_Variable> vars = new ArrayList<Node_Variable>();
		for (RIFVar var : fa.universalVars()) vars.add(var.getNode());
		return vars;
	}
	
	/**
	 * @param fa 
	 */
	public RIFForAllBindingConsequence(RIFForAll fa, String ruleID) {
		super(extractBindings(fa), ruleID);
	}

}
