package org.openimaj.rif.conditions.data.datum;

import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.formula.RIFFormula;

/**
 * @author david.monks
 *
 */
public class RIFExternal implements RIFDatum, RIFFormula {

	private RIFAtom command;
	
	public RIFExternal(){
		
	}
	
	public void setCommand(RIFAtom c){
		this.command = c;
	}
	
	public RIFAtom getCommand(){
		return this.command;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF external statement.");
	}
	
}
