package org.openimaj.rif.conditions.formula;

import org.openimaj.rif.conditions.data.RIFData;

/**
 * @author david.monks
 *
 */
public class RIFEqual implements RIFFormula {

	private RIFData left;
	private RIFData right;
	
	/**
	 * 
	 */
	public RIFEqual(){
		
	}
	
	/**
	 * @param l
	 */
	public void setLeft(RIFData l){
		this.left = l;
	}
	
	/**
	 * @param r
	 */
	public void setRight(RIFData r){
		this.right = r;
	}
	
	/**
	 * @return
	 */
	public RIFData getRight(){
		return this.right;
	}
	
	/**
	 * @return
	 */
	public RIFData getLeft(){
		return this.left;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF equality statement.");
	}
	
}
