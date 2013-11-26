package org.openimaj.squall.compile.rif.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.data.RIFDatum;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NumericGreaterThanProvider extends ExternalFunctionProvider {

	private static final class NumericGreaterThanFunction extends NumericRIFPredicateFunction {

		private Node[] nodes;
		private static final Logger logger = Logger.getLogger(NumericGreaterThanFunction.class);

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
			logger  .debug(String.format("Context(%s) sent to Predicate(%s > %s)" , in, this.nodes[0], this.nodes[1]));
			List<Context> ret = new ArrayList<Context>();
			Map<String,Node> binds = in.getTyped("bindings");
			Double first = extractBinding(binds, nodes[0]);
			Double second = extractBinding(binds, nodes[1]);
			if(first > second) {
				logger  .debug(String.format("numeric greater than check Passed!"));
				ret.add(in);
			}
			return ret;
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

}
