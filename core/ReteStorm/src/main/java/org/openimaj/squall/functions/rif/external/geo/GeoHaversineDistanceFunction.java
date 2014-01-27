package org.openimaj.squall.functions.rif.external.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.rif.calculators.NumericRIFValueFunction;
import org.openimaj.squall.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class GeoHaversineDistanceFunction extends NumericRIFValueFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9023811754649220610L;
	private static final Logger logger = Logger.getLogger(GeoHaversineDistanceFunction.class);
	
	private static final long earthRadius = 6371;//kilometres
	private static final String DEFAULT_RESULT_VAR = "geoHaversineDistance";
	
	/**
	 * @param ns
	 * @throws RIFPredicateException
	 */
	public GeoHaversineDistanceFunction(Node[] ns) throws RIFPredicateException {
		this(ns, (Node_Variable) NodeFactory.createVariable(DEFAULT_RESULT_VAR));
	}
	
	/**
	 * @param ns
	 * @param rn 
	 * @throws RIFPredicateException
	 */
	public GeoHaversineDistanceFunction(Node[] ns, Node_Variable rn) throws RIFPredicateException {
		super(ns, rn);
		if (ns.length < 4){
			throw new RIFPredicateException("Too few values defined for HaversineDistance predicate:\nUsage: geoHaversineDistance(point A lat, point A long, point B lat, point B long)");
		}
	}
	
	private GeoHaversineDistanceFunction() // required for deserialisation by reflection
			throws RIFPredicateException{
		super(new Node[]{
				NodeFactory.createVariable("foo"),
				NodeFactory.createVariable("bar")
		}, (Node_Variable) NodeFactory.createVariable(DEFAULT_RESULT_VAR));
	}

	private double haversin(double pheta){
		return (1d - Math.cos(pheta)) / 2d;
	}

	@Override
	public List<Context> apply(Context in) {
		logger.debug(String.format("Context(%s) sent to Value(haversine(%s,%s,%s,%s))" , in, super.nodes[0], super.nodes[1], super.nodes[2], super.nodes[3]));
		List<Context> ret = new ArrayList<Context>();
		Map<String,Node> binds = in.getTyped("bindings");
		
		Double lat1 = Math.PI * super.extractBinding(binds, super.nodes[0]) / 180d;
		Double long1 = Math.PI * super.extractBinding(binds, super.nodes[1]) / 180d;
		Double lat2 = Math.PI * super.extractBinding(binds, super.nodes[2]) / 180d;
		Double long2 = Math.PI * super.extractBinding(binds, super.nodes[3]) / 180d;
		
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
		newC.put("bindings", newB);
		ret.add(newC);
		
		return ret;
	}

	@Override
	public String identifier(Map<String, String> varmap) {
		StringBuilder anon = new StringBuilder("GeoHaversineDistance(");
		anon.append(super.mapNode(varmap, super.nodes[0])).append(",")
			.append(super.mapNode(varmap, super.nodes[1])).append(",")
			.append(super.mapNode(varmap, super.nodes[2])).append(",")
			.append(super.mapNode(varmap, super.nodes[3]));
		anon.append(")");
		return anon.toString();
	}

	@Override
	public String identifier() {
		StringBuilder anon = new StringBuilder("GeoHaversineDistance(");
		anon.append(super.stringifyNode(super.nodes[0])).append(",")
			.append(super.stringifyNode(super.nodes[1])).append(",")
			.append(super.stringifyNode(super.nodes[2])).append(",")
			.append(super.stringifyNode(super.nodes[3]));
		anon.append(")");
		return anon.toString();
	}
	
}
