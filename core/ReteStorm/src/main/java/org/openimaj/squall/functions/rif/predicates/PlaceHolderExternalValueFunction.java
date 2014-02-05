package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFDatum;
import org.openimaj.rifcore.conditions.data.RIFList;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class PlaceHolderExternalValueFunction extends BasePredicateFunction {
	
	private static Node[] extractArguments(RIFExternalValue expr) {
		List<Node> vars = new ArrayList<Node>();
		RIFAtom command = expr.getVal();
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
	public PlaceHolderExternalValueFunction(RIFExternalValue expr) throws RIFPredicateException {
		super(extractArguments(expr), new HashMap<Node, BaseValueFunction>());
		Node_Concrete op = expr.getVal().getOp().getNode();
		this.name = op.isLiteral()
						? op.getLiteralValue().toString()
						: op.isURI()
							? op.getURI()
							: "_:";
	}

	@Override
	protected List<Context> applyRoot(Context in) {
		return null;
	}

}
