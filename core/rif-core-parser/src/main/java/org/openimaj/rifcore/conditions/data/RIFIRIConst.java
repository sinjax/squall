package org.openimaj.rifcore.conditions.data;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFIRIConst extends RIFURIConst {
	
	/**
	 * 
	 */
	public static final String datatype = "http://www.w3.org/2007/rif#iri";
	
	@Override
	public String getDatatype(){
		return RIFIRIConst.datatype;
	}
	
}
