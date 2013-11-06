package org.openimaj.rif.conditions.atomic;

import org.openimaj.rif.conditions.formula.RIFFormula;
import org.openimaj.rif.rules.RIFStatement;
import org.openimaj.rif.rules.RIFSentence;

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
