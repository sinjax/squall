package org.openimaj.rif.conditions.formula;

import org.openimaj.rif.conditions.RIFExternal;
import org.openimaj.rif.conditions.atomic.RIFAtom;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFExternalValue implements RIFExternal, RIFFormula {
	
	private RIFAtom val;
	
	/**
	 * @param a 
	 */
	public void setVal(RIFAtom a){
		this.val = a;
	}
	
	/**
	 * @return
	 */
	public RIFAtom getVal(){
		return this.val;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF external statement.");
	}
	
}
