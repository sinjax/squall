package org.openimaj.rif.conditions.data;


import com.hp.hpl.jena.graph.Node;

/**
 * @author david.monks
 *
 */
public interface RIFDatum extends RIFData {
	
	/**
	 * @return
	 * 		The Jena Node representing this Datum 
	 */
	public Node getNode();
	
}
