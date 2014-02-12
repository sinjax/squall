package org.openimaj.squall.compile.data;

import java.util.Map;

import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public class RuleWrappedFunction<T extends IFunction<Context, Context>> extends
		RuleWrapped<T> implements Cloneable {

	/**
	 * @param arvh
	 */
	public RuleWrappedFunction(ARVHComponent arvh) {
		super(arvh);
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static abstract class ARVHComponent extends AnonimisedRuleVariableHolder {
		
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

}
