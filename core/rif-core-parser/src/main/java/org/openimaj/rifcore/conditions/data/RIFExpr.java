package org.openimaj.rifcore.conditions.data;

import java.util.List;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFExpr extends RIFFunction {

	private RIFAtom command;
	private Node_Variable node;
	
	/**
	 * @param c
	 * @param list 
	 */
	public void setCommand(RIFAtom c, List<String> list){
		this.command = c;
		
		String name = "Expr("+c.toString()+")";
		while (list.contains(name)) name += "*";
		this.node = (Node_Variable) NodeFactory.createVariable(name);
		list.add(name);
	}
	
	/**
	 * @return
	 */
	public RIFAtom getCommand(){
		return this.command;
	}

	@Override
	public Node_Variable getNode() {
		return this.node;
	}
	
}
