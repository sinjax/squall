package org.openimaj.rif.conditions.data;

import java.util.Set;

import org.openimaj.rif.conditions.atomic.RIFAtom;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFExpr implements RIFFunction {

	private RIFAtom command;
	private Node_RuleVariable node;
	
	/**
	 * 
	 */
	public RIFExpr(){
	}
	
	/**
	 * @param c
	 * @param names 
	 */
	public void setCommand(RIFAtom c, Set<String> names){
		this.command = c;
		
		String name = "Expr ( "+c.toString()+" )";
		while (names.contains(name)) name += "*";
		this.node = new Node_RuleVariable(name,names.size());
	}
	
	/**
	 * @return
	 */
	public RIFAtom getCommand(){
		return this.command;
	}

	@Override
	public Node_RuleVariable getNode() {
		return this.node;
	}
	
}
