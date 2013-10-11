package org.openimaj.rif.conditions.formula;

import org.openimaj.rif.conditions.data.RIFData;

/**
 * @author david.monks
 *
 */
public class RIFMember implements RIFFormula {
	
	private RIFData instance;
	private RIFData inClass;
	
	/**
	 * 
	 */
	public RIFMember(){
		
	}
	
	/**
	 * @param i
	 */
	public void setInstance(RIFData i){
		this.instance = i;
	}
	
	/**
	 * @param c
	 */
	public void setInClass(RIFData c){
		this.inClass = c;
	}
	
	/**
	 * @return
	 */
	public RIFData getInstance(){
		return this.instance;
	}
	
	/**
	 * @return
	 */
	public RIFData getInClass(){
		return this.inClass;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF membership statement.");
	}

}
