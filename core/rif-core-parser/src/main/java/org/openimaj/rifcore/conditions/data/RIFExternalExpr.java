package org.openimaj.rifcore.conditions.data;

import org.openimaj.rifcore.conditions.RIFExternal;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFExternalExpr extends RIFFunction implements RIFExternal {

	private RIFExpr expr;
	
	/**
	 * @param expr 
	 */
	public void setExpr(RIFExpr expr){
		this.expr = expr;
	}
	
	/**
	 * @return
	 */
	public RIFExpr getExpr(){
		return this.expr;
	}

	@Override
	public Node_Variable getNode() {
		return this.expr.getNode();
	}
	
}
