package org.openimaj.squall.compile.rif.provider.consequences;

import org.openimaj.squall.functions.rif.consequences.RIFAtomConsequence;
import org.openimaj.squall.functions.rif.consequences.RIFAtomConsequence.RuleWrappedRIFAtomConsequence;

import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFAtomConsequenceProvider extends
		BaseConsequenceProvider<Functor> {

	/**
	 * @param rID
	 */
	public RIFAtomConsequenceProvider(String rID){
		super(rID);
	}
	
	@Override
	public RuleWrappedRIFAtomConsequence apply(Functor in) {
		return RIFAtomConsequence.ruleWrapped(in, super.getRuleID());
	}

}