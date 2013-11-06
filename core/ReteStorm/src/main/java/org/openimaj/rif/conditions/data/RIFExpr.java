package org.openimaj.rif.conditions.data;

import java.util.List;
import org.openimaj.rif.conditions.atomic.RIFAtom;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFExpr extends RIFFunction {

	private RIFAtom command;
	private Node_RuleVariable node;
	
	/**
	 * 
	 */
	public RIFExpr(){
	}
	
	/**
	 * @param c
	 * @param list 
	 */
	public void setCommand(RIFAtom c, List<String> list){
		this.command = c;
		
		String name = "Expr("+c.toString()+")";
		while (list.contains(name)) name += "*";
		this.node = new Node_RuleVariable(name,list.size());
		list.add(name);
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
