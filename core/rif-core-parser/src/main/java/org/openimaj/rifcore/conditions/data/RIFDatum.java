package org.openimaj.rifcore.conditions.data;


import com.hp.hpl.jena.graph.Node;

/**
 * @author david.monks
 *
 */
public abstract class RIFDatum extends RIFData {
	
	/**
	 * @return
	 * 		The Jena Node representing this Datum 
	 */
	public abstract Node getNode();
	
}
