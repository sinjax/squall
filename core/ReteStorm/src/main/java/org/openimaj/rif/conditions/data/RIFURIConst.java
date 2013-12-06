package org.openimaj.rif.conditions.data;

import java.net.URI;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class RIFURIConst extends RIFConst<URI> {

	@Override
	public void setData(URI data) {
		this.node = (Node_Concrete) NodeFactory.createURI(data.toString());
	}
	
}
