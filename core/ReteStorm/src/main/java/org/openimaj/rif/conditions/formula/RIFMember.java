package org.openimaj.rif.conditions.formula;

import org.openimaj.rif.conditions.data.datum.RIFDatum;

/**
 * @author david.monks
 *
 */
public class RIFMember implements RIFFormula {
	
	private RIFDatum instance;
	private RIFDatum inClass;
	
	/**
	 * 
	 */
	public RIFMember(){
		
	}
	
	/**
	 * @param i
	 */
	public void setInstance(RIFDatum i){
		this.instance = i;
	}
	
	/**
	 * @param c
	 */
	public void setInClass(RIFDatum c){
		this.inClass = c;
	}
	
	/**
	 * @return
	 */
	public RIFDatum getInstance(){
		return this.instance;
	}
	
	/**
	 * @return
	 */
	public RIFDatum getInClass(){
		return this.inClass;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF membership statement.");
	}

}
