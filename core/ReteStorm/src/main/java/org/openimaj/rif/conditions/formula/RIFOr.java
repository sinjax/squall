package org.openimaj.rif.conditions.formula;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author david.monks
 *
 */
public class RIFOr implements RIFFormula, Iterable<RIFFormula> {
	
	private Set<RIFFormula> formuli;
	
	/**
	 * 
	 */
	public RIFOr(){
		this.formuli = new HashSet<RIFFormula>();
	}
	
	/**
	 * @param f
	 */
	public void addFormula(RIFFormula f){
		this.formuli.add(f);
	}
	
	@Override
	public Iterator<RIFFormula> iterator(){
		return this.formuli.iterator();
	}
	
}
