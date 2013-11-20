package org.openimaj.squall.compile.rif.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.data.RIFDatum;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NumericGreaterThanProvider extends ExternalFunctionProvider {

	private final class NumericGreaterThanFunction extends BaseRIFPredicateFunction {

		private Node[] nodes;

		public NumericGreaterThanFunction(Node[] ns) throws RIFPredicateException {
			super(ns);
			this.nodes = ns;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -7935891899097417140L;

		@Override
		public List<Context> apply(Context in) {
			List<Context> ret = new ArrayList<Context>();
			Map<String,Node> binds = in.getTyped("bindings");
			Double first = extractBinding(binds, nodes[0]);
			Double second = extractBinding(binds, nodes[1]);
			if(first > second) ret.add(in);
			return ret;
		}

		private Double extractBinding(Map<String, Node> binds, Node node) {
			if(node.isVariable()){
				node = binds.get(node.getName());
				if(node == null){
					throw new UnsupportedOperationException("Unbound variable");
				}
			}
			try{					
				if(node.isLiteral()){
					Node_Literal lit = (Node_Literal) node;
					
					return ((Number)lit.getLiteralValue()).doubleValue();
				}
				throw new UnsupportedOperationException("Incorrect datatype for numeric comparison");
			}
			catch (ClassCastException e){
				throw new UnsupportedOperationException("Incorrect datatype for numeric comparison");
			}
		}
		
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			return new NumericGreaterThanFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			return new NumericGreaterThanFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	private Node[] extractNodes(RIFAtom atom) {
		List<Node> nodes = new ArrayList<Node>();
		for (RIFDatum node : atom) {
			nodes.add(node.getNode());
		}
		return nodes.toArray(new Node[nodes.size()]);
	}

	

	

}
