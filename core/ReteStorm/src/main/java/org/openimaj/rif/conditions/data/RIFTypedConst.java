package org.openimaj.rif.conditions.data;

import java.net.URI;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author david.monks
 *
 */
public class RIFTypedConst extends RIFStringConst {
	
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
		this.node = (Node_Concrete) Node.createLiteral(data, Node.createLiteral(data+"^^"+dtype.toString()).getLiteralDatatype());
	}

}
