package org.openimaj.rif.conditions.data;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <T> 
 *
 */
public abstract class RIFConst <T> implements RIFDatum {

	private static final String datatype = "#anySimpleType";
	
	protected Node_Concrete node;
	
	/**
	 * 
	 */
	public RIFConst(){
		this.node = null;
	}
	
	/**
	 * @param data
	 */
	public abstract void setData(T data);
	
	/**
	 * @return
	 */
	public Node_Concrete getNode(){
		return this.node;
	}
	
	/**
	 * @return
	 */
	public String getDatatype(){
		return this.node == null
					? XSDDatatype.XSD+RIFConst.datatype
					: this.node.isLiteral()
						? this.node.getLiteralDatatype().getURI()
						: XSDDatatype.XSDstring.getURI();
	}
	
}
