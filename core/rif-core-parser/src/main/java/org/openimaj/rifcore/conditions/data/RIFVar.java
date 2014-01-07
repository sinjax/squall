package org.openimaj.rifcore.conditions.data;

import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author david.monks
 *
 */
public class RIFVar extends RIFDatum {
	
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
