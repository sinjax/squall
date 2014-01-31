package org.openimaj.squall.compile.functions.rif.external.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.rif.provider.RIFExprFunctionRegistry;
import org.openimaj.squall.compile.rif.provider.RIFExternalFunctionProvider;
import org.openimaj.squall.functions.rif.external.geo.GeoHaversineDistanceFunction;
import org.openimaj.squall.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
import org.openimaj.util.pair.IndependentPair;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class GeoInHaversineDistanceProvider extends RIFExternalFunctionProvider {

	private static final class GeoInHaversineDistanceFunction extends NumericRIFPredicateFunction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7445044998530563542L;
		private static final Logger logger = Logger.getLogger(GeoInHaversineDistanceFunction.class);
		
		private GeoHaversineDistanceFunction haversineFunc;
		
		public GeoInHaversineDistanceFunction(Node[] ns)
				throws RIFPredicateException {
			super(new Node[]{
				ns[0],
				NodeFactory.createVariable("placeHolder")
			});
			if (ns.length < 5){
				throw new RIFPredicateException("Too few values defined for InHaversineDistance predicate:\nUsage: geoInHaversineDistance(dist in km, point A lat, point A long, point B lat, point B long)");
			}
			this.haversineFunc = new GeoHaversineDistanceFunction(new Node[]{
				ns[1],ns[2],ns[3],ns[4]
			});
		}
		
		private GeoInHaversineDistanceFunction() // required for deserialisation by reflection
				throws RIFPredicateException{
			super(new Node[]{
					NodeFactory.createLiteral("foo"),
					NodeFactory.createVariable("bar")
			});
		}
		
		@Override
		public void setSourceVariables(AnonimisedRuleVariableHolder arvh) {
			super.setSourceVariables(arvh);
			this.haversineFunc.setSourceVariables(arvh);
			super.nodes[1] = this.haversineFunc.getResultVarNode();
		}

		@Override
		public List<Context> apply(Context in) {
			logger  .debug(String.format("Context(%s) sent to Predicate(haversine(lat,long,lat,long) < %s)" , in, super.nodes[0]));
			List<Context> ret = new ArrayList<Context>();
			Map<String,Node> binds = in.getTyped(ContextKey.BINDINGS_KEY.toString());
			
			Double maxDist = super.extractBinding(binds, super.nodes[0]);//kilometres
			
			List<Context> results = this.haversineFunc.apply(in);
			binds = results.get(0).getTyped(ContextKey.BINDINGS_KEY.toString());
			
			if (super.extractBinding(binds, this.haversineFunc.getResultVarNode()) <= maxDist) {
				logger  .debug(String.format("GeoHaversine check Passed!"));
				ret.add(in);
			}
			
			return ret;
		}

		@Override
		public String identifier(Map<String, String> varmap) {
			StringBuilder anon = new StringBuilder("GeoInHaversineDistance(");
			anon.append(super.mapNode(varmap, super.nodes[0])).append(" > ")
				.append(this.haversineFunc.identifier(varmap));
			anon.append(")");
			return anon.toString();
		}

		@Override
		public String identifier() {
			Map<String, String> funcToSubFuncVars= new HashMap<String, String>();
			funcToSubFuncVars.put("0","1");
			funcToSubFuncVars.put("1","2");
			funcToSubFuncVars.put("2","3");
			funcToSubFuncVars.put("3","4");
			funcToSubFuncVars.put("4","5");
			
			StringBuilder anon = new StringBuilder("GeoInHaversineDistance(");
			anon.append(super.stringifyNode(super.nodes[0])).append(" > ")
				.append(this.haversineFunc.identifier(funcToSubFuncVars));
			anon.append(")");
			return anon.toString();
		}
		
		@Override
		public void write(Kryo kryo, Output output) {
			super.write(kryo, output);
			kryo.writeClassAndObject(output, this.haversineFunc);
		}
		
		@Override
		public void read(Kryo kryo, Input input) {
			super.read(kryo, input);
			this.haversineFunc = (GeoHaversineDistanceFunction) kryo.readClassAndObject(input);
		}
		
	}
	
	/**
	 * @param reg
	 */
	public GeoInHaversineDistanceProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	@Override
	public IVFunction<Context, Context> apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			IndependentPair<Node[], IVFunction<Context,Context>[]> data = extractNodesAndSubFunctions(atom);
			return new GeoInHaversineDistanceFunction(data.firstObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			IndependentPair<Node[], IVFunction<Context,Context>[]> data = extractNodesAndSubFunctions(atom);
			return new GeoInHaversineDistanceFunction(data.firstObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
