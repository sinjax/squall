package org.openimaj.rif.conditions.data.datum;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author david.monks
 *
 */
public class RIFStringConst extends RIFConst<String> {
	
	/**
	 * 
	 */
	public static final String datatype = XSDDatatype.XSDstring.getURI();

	@Override
	public void setData(String data) {
		this.node = (Node_Concrete) Node.createLiteral(data, XSDDatatype.XSDstring);
	}

}
