package org.openimaj.rif.conditions.data.datum;

import java.net.URI;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author david.monks
 *
 */
public class RIFIRIConst extends RIFConst<URI> {
	
	/**
	 * 
	 */
	public static final String datatype = "http://www.w3.org/2007/rif#iri";

	@Override
	public void setData(URI data) {
		this.node = (Node_Concrete) Node.createURI(data.toString());
	}
	
	@Override
	public String getDatatype(){
		return RIFIRIConst.datatype;
	}
	
}
