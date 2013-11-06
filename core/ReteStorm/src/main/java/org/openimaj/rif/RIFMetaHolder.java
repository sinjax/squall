package org.openimaj.rif;

import org.openimaj.rif.conditions.data.RIFIRIConst;
import org.openimaj.rif.conditions.formula.RIFFormula;
import org.xml.sax.SAXException;

/**
 * The methods required of RIF elelments that hold meta data information
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class RIFMetaHolder {

	private RIFIRIConst id;
	private RIFFormula meta;
	
	/**
	 * 
	 */
	public RIFMetaHolder(){
		
	}
	
	/**
	 * Set the ID of the element to the value of 'i'.
	 * @param i
	 */
	public void setID(RIFIRIConst i){
		this.id = i;
	}
	
	/**
	 * @param f
	 * @throws SAXException
	 */
	public void setMetadata(RIFFormula f) throws SAXException{
		this.meta = f;
	}
	
	/**
	 * @return
	 */
	public RIFIRIConst getID(){
		return this.id;
	}
	
	/**
	 * @return
	 */
	public RIFFormula getMetadata(){
		return this.meta;
	}
	
}
