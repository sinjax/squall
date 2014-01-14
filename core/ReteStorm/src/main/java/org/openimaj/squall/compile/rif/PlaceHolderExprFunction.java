package org.openimaj.squall.compile.rif;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFDatum;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFList;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class PlaceHolderExprFunction extends BaseRIFPredicateFunction {
	
	private static Node[] extractArguments(RIFExpr expr) {
		List<Node> vars = new ArrayList<Node>();
		RIFAtom command = expr.getCommand();
		for (int i = 0; i < command.getArgsSize(); i++){
			if (command.getArg(i) instanceof RIFList){
				RIFList list = (RIFList) command.getArg(i);
				for (int j = 0; j < list.size(); j ++){
					if (command.getArg(i) instanceof RIFDatum){
						RIFDatum datum = (RIFDatum)command.getArg(i);
						if (datum.getNode().isVariable())
							vars.add(datum.getNode());
					}
				}
			} else if (command.getArg(i) instanceof RIFDatum){
				RIFDatum datum = (RIFDatum)command.getArg(i);
				if (datum.getNode().isVariable())
					vars.add(datum.getNode());
			}
		}
		return vars.toArray(new Node[vars.size()]);
	}
	
	private String name;
	
	/**
	 * @param expr 
	 * @throws RIFPredicateException 
	 */
	public PlaceHolderExprFunction(RIFExpr expr) throws RIFPredicateException {
		super(extractArguments(expr));
		Node_Concrete op = expr.getCommand().getOp().getNode();
		this.name = op.isLiteral()
						? op.getLiteralValue().toString()
						: op.isURI()
							? op.getURI()
							: "_:";
	}

	@Override
	public List<Context> apply(Context in) {
		return null;
	}

	@Override
	public String identifier() {
		StringBuilder anon = new StringBuilder();
		if (super.varHolder != null){
			anon.append(super.varHolder.identifier());
		}
		anon.append("PlaceHolder:").append(this.name).append("(");
		if (super.nodes.length > 0){
			int i = 0;
			anon.append(super.stringifyNode(super.nodes[i]));
			for (i++; i < this.varCount(); i++){
				anon.append(",").append(super.stringifyNode(super.nodes[i]));
			}
		}
		return anon.append(")").toString();
	}

	@Override
	public String identifier(Map<String, String> varmap) {
		StringBuilder anon = new StringBuilder();
		if (super.varHolder != null){
			anon.append(super.varHolder.identifier(varmap));
		}
		anon.append("PlaceHolder:").append(this.name).append("(");
		if (super.nodes.length > 0){
			int i = 0;
			anon.append(super.mapNode(varmap, super.nodes[i]));
			for (i++; i < this.varCount(); i++){
				anon.append(",").append(super.mapNode(varmap, super.nodes[i]));
			}
		}
		return anon.append(")").toString();
	}

}
