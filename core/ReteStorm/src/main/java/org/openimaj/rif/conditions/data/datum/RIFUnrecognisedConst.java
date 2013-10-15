package org.openimaj.rif.conditions.data.datum;

import java.net.URI;

/**
 * @author david.monks
 *
 */
public class RIFUnrecognisedConst extends RIFConst<String> {
	
	private final URI dtype; 
	
	/**
	 * @param dtype
	 */
	public RIFUnrecognisedConst(URI dtype){
		this.dtype = dtype;
	}
	
	@Override
	public String getDatatype(){
		return this.dtype.toString();
	}

}
