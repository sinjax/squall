package org.openimaj.squall.functions.calculators.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.NumericValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.predicates.NumericPredicateFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class GeoHaversineDistanceFunction extends NumericValueFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9023811754649220610L;
	private static final Logger logger = Logger.getLogger(GeoHaversineDistanceFunction.class);
	
	private static final long earthRadius = 6371;//kilometres
	private static final String DEFAULT_RESULT_VAR = "geoHaversineDistance";
	
	/**
	 * @param ns
	 * @param rn
	 * @param funcMap
	 * @return
	 * @throws RIFPredicateException 
	 */
	public static RuleWrappedGeoHaversineDistanceFunction ruleWrapped(
																Node[] ns,
																Node_Variable rn,
																Map<Node, RuleWrappedValueFunction<?>> funcMap
															) throws RIFPredicateException{
		return new RuleWrappedGeoHaversineDistanceFunction(ns, rn, funcMap);
	}
	
	/**
	 * @param ns
	 * @param rn 
	 * @param funcs 
	 * @throws RIFPredicateException
	 */
	public GeoHaversineDistanceFunction(Node[] ns, Node_Variable rn, Map<Node, BaseValueFunction> funcs) throws RIFPredicateException {
		super(ns, rn, funcs);
		if (ns.length < 4){
			throw new RIFPredicateException("Too few values defined for HaversineDistance predicate:\nUsage: geoHaversineDistance(point A lat, point A long, point B lat, point B long)");
		}
	}
	
	private GeoHaversineDistanceFunction() // required for deserialisation by reflection
			throws RIFPredicateException{
		super(new Node[]{
				NodeFactory.createVariable("foo"),
				NodeFactory.createVariable("bar")
			},
			(Node_Variable) NodeFactory.createVariable(DEFAULT_RESULT_VAR),
			new HashMap<Node, BaseValueFunction>()
		);
	}
	
	@Override
	public GeoHaversineDistanceFunction clone() {
		try {
			return new GeoHaversineDistanceFunction(super.getNodes(), super.getResultVarNode(), super.getFuncMap());
		} catch (RIFPredicateException e) {
			throw new RuntimeException("Clone of valid function should not be invalid.", e);
		}
	}

	private double haversin(double pheta){
		return (1d - Math.cos(pheta)) / 2d;
	}

	@Override
	public List<Context> applyRoot(Context in) {
		logger.debug(String.format("Context(%s) sent to haversine(%s,%s,%s,%s)" , in, super.getNode(0), super.getNode(1), super.getNode(2), super.getNode(3)));
		List<Context> ret = new ArrayList<Context>();
		Map<String,Node> binds = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		Double lat1 = Math.PI * super.extractBinding(binds, 0) / 180d;
		Double long1 = Math.PI * super.extractBinding(binds, 1) / 180d;
		Double lat2 = Math.PI * super.extractBinding(binds, 2) / 180d;
		Double long2 = Math.PI * super.extractBinding(binds, 3) / 180d;
		
		Double distance = 2 * earthRadius * Math.asin(
												Math.sqrt(
													haversin(lat2 - lat1) +
													Math.cos(lat1) * Math.cos(lat2) * haversin(long2 - long1)
												)
											);
		
		Context newC = new Context();
		Map<String, Node> newB = new HashMap<String, Node>();
		newB.putAll(binds);
		newB.put(
				super.getResultVarNode().getName(),
				NodeFactory.createLiteral(Double.toString(distance), XSDDatatype.XSDdouble)
		);
		newC.put(ContextKey.BINDINGS_KEY.toString(), newB);
		ret.add(newC);
		
		return ret;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedGeoHaversineDistanceFunction extends RuleWrappedValueFunction<GeoHaversineDistanceFunction> {

		protected RuleWrappedGeoHaversineDistanceFunction(Node[] ns, Node_Variable rn, Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super("HaversineDistance", ns, rn, funcMap);
			this.wrap(new GeoHaversineDistanceFunction(ns, rn, super.getRulelessFuncMap()));
		}
		
	}
	
}
