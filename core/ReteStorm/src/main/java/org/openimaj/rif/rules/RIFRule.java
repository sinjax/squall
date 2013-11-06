package org.openimaj.rif.rules;

import org.openimaj.rif.conditions.formula.RIFFormula;

/**
 * @author david.monks
 *
 */
public class RIFRule extends RIFStatement {
	
	private RIFFormula head;
	private RIFFormula body;
	
	/**
	 * 
	 */
	public RIFRule(){
		super();
	}
	
	/**
	 * @param formula
	 */
	public void setBody(RIFFormula formula){
		this.body = formula;
	}
	
	/**
	 * @param formula
	 */
	public void setHead(RIFFormula formula){
		this.head = formula;
	}
	
	/**
	 * @return
	 */
	public RIFFormula getBody(){
		return this.body;
	}
	
	/**
	 * @return
	 */
	public RIFFormula getHead(){
		return this.head;
	}


}
