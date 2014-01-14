package org.openimaj.squall.compile.rif.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;

import cern.colt.Arrays;

import com.hp.hpl.jena.graph.Node;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NumericGreaterThanProvider extends ExternalFunctionProvider {

	private static final class NumericGreaterThanFunction extends NumericRIFPredicateFunction {

		private static final Logger logger = Logger.getLogger(NumericGreaterThanFunction.class);

		public NumericGreaterThanFunction(Node[] ns) throws RIFPredicateException {
			super(ns);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -7935891899097417140L;

		@Override
		public List<Context> apply(Context in) {
			logger  .debug(String.format("Context(%s) sent to Predicate >%s" , in, Arrays.toString(super.nodes)));
			List<Context> ret = new ArrayList<Context>();
			Map<String,Node> binds = in.getTyped("bindings");
			
			Double current = super.extractBinding(binds, super.nodes[0]);
			Double next = super.extractBinding(binds, super.nodes[1]);
			for (int i = 2; i < super.nodes.length; i++){
				if(current <= next) {
					logger  .debug(String.format("Numeric Greater Than check failed on comparison"));
					return ret;
				}
				current = next;
				next = super.extractBinding(binds, super.nodes[i]);
			}
			ret.add(in);
			return ret;
		}

		@Override
		public String identifier(Map<String, String> varmap) {
			StringBuilder anon = new StringBuilder();
			if (this.varHolder != null){
				anon.append(super.varHolder.identifier(varmap));
			}
			anon.append("NumericGreaterThan(");
			if (super.nodes.length > 0){
				int i = 0;
				anon.append(super.mapNode(varmap, super.nodes[i]));
				for (i++; i < super.nodes.length; i++){
					anon.append(",").append(super.mapNode(varmap, super.nodes[i]));
				}
			}
			return anon.append(")").toString();
		}

		@Override
		public String identifier() {
			StringBuilder anon = new StringBuilder();
			if (this.varHolder != null){
				anon.append(super.varHolder.identifier());
			}
			anon.append("NumericGreaterThan(");
			if (super.nodes.length > 0){
				int i = 0;
				anon.append(super.stringifyNode(super.nodes[i]));
				for (i++; i < super.nodes.length; i++){
					anon.append(",").append(super.stringifyNode(super.nodes[i]));
				}
			}
			return anon.append(")").toString();
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
