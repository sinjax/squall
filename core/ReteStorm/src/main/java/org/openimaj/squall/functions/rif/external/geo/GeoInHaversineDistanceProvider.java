package org.openimaj.squall.functions.rif.external.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionProvider;
import org.openimaj.squall.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class GeoInHaversineDistanceProvider extends ExternalFunctionProvider {

	private static final class GeoInHaversineDistanceFunction extends NumericRIFPredicateFunction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7445044998530563542L;
		private static final long earthRadius = 6371;//kilometres
		private static final Logger logger = Logger.getLogger(GeoInHaversineDistanceFunction.class);
		
		public GeoInHaversineDistanceFunction(Node[] ns)
				throws RIFPredicateException {
			super(ns);
		}
		
		private double haversin(double pheta){
			return (1d - Math.cos(pheta)) / 2d;
		}

		@Override
		public List<Context> apply(Context in) {
			logger  .debug(String.format("Context(%s) sent to Predicate(haversine(%s,%s,%s,%s) < %s)" , in, super.nodes[1], super.nodes[2], super.nodes[3], super.nodes[4], super.nodes[0]));
			List<Context> ret = new ArrayList<Context>();
			Map<String,Node> binds = in.getTyped("bindings");
			
			Double maxDist = super.extractBinding(binds, super.nodes[0]);//kilometres
			Double lat1 = Math.PI * super.extractBinding(binds, super.nodes[1]) / 180d;
			Double long1 = Math.PI * super.extractBinding(binds, super.nodes[2]) / 180d;
			Double lat2 = Math.PI * super.extractBinding(binds, super.nodes[3]) / 180d;
			Double long2 = Math.PI * super.extractBinding(binds, super.nodes[4]) / 180d;
			
			Double distance = 2 * earthRadius * Math.asin(
													Math.sqrt(
														haversin(lat2 - lat1) +
														Math.cos(lat1) * Math.cos(lat2) * haversin(long2 - long1)
													)
												);
			if (distance <= maxDist) {
				logger  .debug(String.format("GeoHaversine check Passed!"));
				ret.add(in);
			}
			
			return ret;
		}

		@Override
		public String identifier(Map<String, String> varmap) {
			StringBuilder anon = new StringBuilder("GeoInHaversineDistance(");
			anon.append(super.mapNode(varmap, super.nodes[0])).append(",")
				.append(super.mapNode(varmap, super.nodes[1])).append(",")
				.append(super.mapNode(varmap, super.nodes[2])).append(",")
				.append(super.mapNode(varmap, super.nodes[3])).append(",")
				.append(super.mapNode(varmap, super.nodes[4]));
			anon.append(")");
			return anon.toString();
		}

		@Override
		public String identifier() {
			StringBuilder anon = new StringBuilder("GeoInHaversineDistance(");
			anon.append(super.stringifyNode(super.nodes[0])).append(",")
				.append(super.stringifyNode(super.nodes[1])).append(",")
				.append(super.stringifyNode(super.nodes[2])).append(",")
				.append(super.stringifyNode(super.nodes[3])).append(",")
				.append(super.stringifyNode(super.nodes[4]));
			anon.append(")");
			return anon.toString();
		}
		
	}
	
	@Override
	public IVFunction<Context, Context> apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			return new GeoInHaversineDistanceFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			return new GeoInHaversineDistanceFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
