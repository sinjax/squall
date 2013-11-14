package org.openimaj.rif.conditions.data;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFLocalConst extends RIFURIConst {
	
	/**
	 * 
	 */
	public static final String datatype = "http://www.w3.org/2007/rif#local";
	
	@Override
	public String getDatatype(){
		return RIFLocalConst.datatype;
	}

}
