package org.openimaj.rif.conditions.data;

import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author david.monks
 *
 */
public class RIFStringConst extends RIFTypedConst {
	
	/**
	 * 
	 */
	public RIFStringConst() {
		super(RIFStringConst.datatypeURI);
	}

	/**
	 * 
	 */
	public static final String datatype;
	private static final XSDDatatype dtype;
	private static final URI datatypeURI;
	static{
		dtype = XSDDatatype.XSDstring;
		datatype = dtype.getURI();
		URI duri;
		try {
			duri = new URI(datatype);
		} catch (URISyntaxException e) {
			duri = null;
		}
		datatypeURI = duri;
	}

	@Override
	public void setData(String data) {
		this.node = (Node_Concrete) Node.createLiteral(data, RIFStringConst.dtype);
	}

}
