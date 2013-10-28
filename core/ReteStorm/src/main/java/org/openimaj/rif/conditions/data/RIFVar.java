package org.openimaj.rif.conditions.data;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author david.monks
 *
 */
public class RIFVar implements RIFDatum {
	
	private Node_RuleVariable node;
	
	/**
	 */
	public RIFVar(){
		this.node = null;
	}
	
	/**
	 * @param name
	 * @param index
	 */
	public void setName(String name, int index){
		this.node = new Node_RuleVariable(name, index);
	}
	
	/**
	 * @return
	 */
	public Node_RuleVariable getNode(){
		return this.node;
	}

}
