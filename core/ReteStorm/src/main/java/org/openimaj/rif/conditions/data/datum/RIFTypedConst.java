package org.openimaj.rif.conditions.data.datum;

import java.net.URI;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

/**
 * @author david.monks
 *
 */
public class RIFTypedConst extends RIFConst<String> {
	
	private final URI dtype; 
	
	/**
	 * @param dtype
	 */
	public RIFTypedConst(URI dtype){
		this.dtype = dtype;
	}
	
	@Override
	public String getDatatype(){
		return this.dtype.toString();
	}

	@Override
	public void setData(String data) {
		Node.createLiteral(data, new XSDDatatype(dtype.toString()));
	}

}
