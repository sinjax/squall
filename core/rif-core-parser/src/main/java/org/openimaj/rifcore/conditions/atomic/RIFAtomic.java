package org.openimaj.rifcore.conditions.atomic;

import org.openimaj.rifcore.conditions.formula.RIFFormula;
import org.openimaj.rifcore.rules.RIFStatement;
import org.openimaj.rifcore.rules.RIFSentence;

/**
 * @author david.monks
 *
 */
public abstract class RIFAtomic extends RIFStatement implements RIFFormula {

	@Override
	public void addFormula(RIFFormula formula){
		throw new UnsupportedOperationException("RIF: Cannot encapsulate any formuli within an atomic RIF statement.");
	}
	
}
