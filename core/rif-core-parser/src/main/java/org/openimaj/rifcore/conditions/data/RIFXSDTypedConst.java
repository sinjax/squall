package org.openimaj.rifcore.conditions.data;

import java.net.URI;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author david.monks
 *
 */
public class RIFXSDTypedConst extends RIFConst<String> {
	
	private final URI dtype; 
	
	/**
	 * @param dtype
	 */
	public RIFXSDTypedConst(URI dtype){
		if(!dtype.toString().startsWith(XSDDatatype.XSD)){
			throw new UnsupportedOperationException("Unsupported dtype");
		}
		this.dtype = dtype;
	}
	
	@Override
	public String getDatatype(){
		return this.dtype.toString();
	}

	@Override
	public void setData(String data) {
		String format = "\"%s\"^^%s";
		this.node = (Node_Concrete) Node.createLiteral(data,new XSDDatatype(this.dtype.getFragment()));
	}

}
