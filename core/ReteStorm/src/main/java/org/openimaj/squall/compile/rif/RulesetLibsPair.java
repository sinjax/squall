package org.openimaj.squall.compile.rif;

import java.util.List;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.squall.functions.rif.RIFExternalFunctionLibrary;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RulesetLibsPair extends
		IndependentPair<RIFRuleSet, List<RIFExternalFunctionLibrary>> {

	/**
	 * @param obj1
	 * @param obj2
	 */
	public RulesetLibsPair(RIFRuleSet obj1,
			List<RIFExternalFunctionLibrary> obj2) {
		super(obj1, obj2);
	}

}
