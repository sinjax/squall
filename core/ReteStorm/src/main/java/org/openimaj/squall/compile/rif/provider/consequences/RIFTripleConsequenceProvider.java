package org.openimaj.squall.compile.rif.provider.consequences;

import org.openimaj.squall.functions.rif.consequences.RIFTripleConsequence;
import org.openimaj.squall.functions.rif.consequences.RIFTripleConsequence.RuleWrappedRIFTripleConsequence;

import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFTripleConsequenceProvider extends
		BaseConsequenceProvider<TriplePattern> {

	/**
	 * @param rID 
	 */
	public RIFTripleConsequenceProvider(String rID) {
		super(rID);
	}

	@Override
	public RuleWrappedRIFTripleConsequence apply(TriplePattern in) {
		return RIFTripleConsequence.ruleWrapped(in, super.getRuleID());
	}

}
