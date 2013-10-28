package org.openimaj.rif.conditions.data;

import org.openimaj.rif.conditions.RIFExternal;

import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFExternalExpr implements RIFExternal, RIFFunction {

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
	public Node_RuleVariable getNode() {
		return this.expr.getNode();
	}
	
}
