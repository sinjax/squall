package org.openimaj.rif.conditions.formula;

import org.openimaj.rif.conditions.data.RIFDatum;

/**
 * @author david.monks
 *
 */
public class RIFEqual implements RIFFormula {

	private RIFDatum left;
	private RIFDatum right;
	
	/**
	 * 
	 */
	public RIFEqual(){
		
	}
	
	/**
	 * @param l
	 */
	public void setLeft(RIFDatum l){
		this.left = l;
	}
	
	/**
	 * @param r
	 */
	public void setRight(RIFDatum r){
		this.right = r;
	}
	
	/**
	 * @return
	 */
	public RIFDatum getRight(){
		return this.right;
	}
	
	/**
	 * @return
	 */
	public RIFDatum getLeft(){
		return this.left;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF equality statement.");
	}
	
}
