package org.openimaj.squall.compile.data.rif;

import java.util.Map;

import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class AbstractRIFFunction extends IVFunction<Context, Context> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7761395895169220137L;

	protected Node registerVariable(Node n, Count count){
		if(n.isVariable()){
			String name = this.getBaseFromRuleVar(n.getName());
			if (name == null){
				count.inc();
				name = Integer.toString(count.getCount());
			}
			Node var = NodeFactory.createVariable(name);
			this.addVariable(name);
			this.putRuleToBaseVarMapEntry(n.getName(), name);
			return var ;
		}
		return n;
	}
	
	protected String stringifyNode(Node node){
		return node.isVariable() ? "?"+node.getName() : node.toString();
	}
	
	protected String mapNode(Map<String,String> varmap, Node node){
		String nodeString = this.stringifyNode(node);
		String mappedString;
		return node.isVariable()
				? (mappedString = varmap.get(nodeString)) == null
					? "VAR"
					: mappedString
				: nodeString;
	}

}
