package org.openimaj.squall.compile.functions.rif.external.geo;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.data.RIFIRIConst;
import org.openimaj.rifcore.conditions.data.RIFStringConst;
import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.InheritsVariables;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.rif.provider.predicates.GeoHaversineDistanceProvider;
import org.openimaj.squall.compile.rif.provider.predicates.NumericGreaterThanProvider;
import org.openimaj.squall.compile.rif.provider.predicates.RIFCoreExprFunctionRegistry;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.rif.external.ExternalLoader;
import org.openimaj.squall.functions.rif.external.geo.GeoHaversineDistanceFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestInHaversineDistanceFunction {
	
	private static final class StubAnonRVarHolder extends AnonimisedRuleVariableHolder {

		public StubAnonRVarHolder(List<Node_Variable> vars){
			super();
			Count count = new Count();
			for (Node_Variable var : vars){
				count.inc();
				this.addVariable(Integer.toString(count.getCount()));
				this.putRuleToBaseVarMapEntry(var.getName(), Integer.toString(count.getCount()));
			}
		}
		
		@Override
		public String identifier(Map<String, String> varmap) {
			// TODO Auto-generated method stub
			return "Stub";
		}

		@Override
		public String identifier() {
			// TODO Auto-generated method stub
			return "Stub";
		}
		
	}
	
	@Test
	public void testPlacesInWorld(){
		double[][] places = new double[][]{
                new double[]{40.7,-74.01}, // Newyork 0
                new double[]{44.6,-63.6}, // Halifax 1
                new double[]{51.3,-0.2}, // London 2
                new double[]{52.5,13.5}, // Berlin 3
                new double[]{55.6,37.8}, // Moscow 4
                new double[]{54.8,82.9}, // Novosbirsk 5
                new double[]{39.8,116.6}, // Beijing 6
                new double[]{35.5,140.0}, // Tokyo 7
                new double[]{-36,174}, // Auckland 8
                new double[]{40.8,-74.11}, // Near NY 9
                new double[]{41.8,-75.11}, // Near NY Further 10
                
                new double[]{51.45,-2.59}, // Bristol
                new double[]{51.12,1.31}, // Dover
        };
		
		//London - New York
		List<Context> ret = performHaversineTest(1000,places[2][0],places[2][1],places[0][0],places[0][1]);
		assertTrue(ret.isEmpty());

		//London - Berlin
		ret = performHaversineTest(1000,places[2][0],places[2][1],places[3][0],places[3][1]);
		assertFalse(ret.isEmpty());
		
		//London - Moscow
		ret = performHaversineTest(1000,places[2][0],places[2][1],places[4][0],places[4][1]);
		assertTrue(ret.isEmpty());
		
		//Aukland - Moscow
		ret = performHaversineTest(1000,places[2][0],places[2][1],places[4][0],places[4][1]);
		assertTrue(ret.isEmpty());
		
		//Bristol - Dover
		ret = performHaversineTest(300,places[11][0],places[11][1],places[12][0],places[12][1]);
		assertFalse(ret.isEmpty());
	}
	
	/**
	 * 
	 */
	@Test
	public void testInHaversineDistance(){
		List<Context> ret = performHaversineTest(6000,140.0,140.0,0.0,0.0);
		System.out.println(ret.toString());
		assertTrue(ret.isEmpty());
	}

	private List<Context> performHaversineTest(double dist, double lat1, double long1, double lat2, double long2) {
		ExternalLoader.loadExternals();
		
		RIFExternalExpr ee = new RIFExternalExpr();
		RIFExpr e = new RIFExpr();
		ee.setExpr(e);
		RIFAtom ae = new RIFAtom();
		e.setCommand(ae, new ArrayList<String>());
		
		RIFIRIConst ce = new RIFIRIConst();
		ae.setOp(ce);
		
		try {
			ce.setData(new URI("http://www.ins.cwi.nl/sib/rif-builtin-function/geo-haversine-distance"));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		RIFVar ve2 = new RIFVar();
		ve2.setName("lat1");
		ae.addArg(ve2);
		RIFVar ve3 = new RIFVar();
		ve3.setName("long1");
		ae.addArg(ve3);
		RIFVar ve4 = new RIFVar();
		ve4.setName("lat2");
		ae.addArg(ve4);
		RIFVar ve5 = new RIFVar();
		ve5.setName("long2");
		ae.addArg(ve5);
		
		RIFExternalValue ev = new RIFExternalValue();
		RIFAtom av = new RIFAtom();
		ev.setVal(av);
		
		RIFStringConst c = new RIFStringConst();
		av.setOp(c);
		
		RIFVar vv1 = new RIFVar();
		vv1.setName("dist");
		av.addArg(vv1);
		av.addArg(ee);
		
		List<Node_Variable> vvars = new ArrayList<Node_Variable>();
		vvars.add(((RIFVar)av.getArg(0)).getNode());
		vvars.add(((RIFVar)ae.getArg(0)).getNode());
		vvars.add(((RIFVar)ae.getArg(1)).getNode());
		vvars.add(((RIFVar)ae.getArg(2)).getNode());
		vvars.add(((RIFVar)ae.getArg(3)).getNode());
		StubAnonRVarHolder stub = new StubAnonRVarHolder(vvars);
		
		RuleWrappedPredicateFunction<? extends BasePredicateFunction> ihdf = (new NumericGreaterThanProvider(RIFCoreExprFunctionRegistry.getRegistry())).apply(ev);
		((InheritsVariables) ihdf).setSourceVariables(stub);
		
		Context cont = new Context();
		Map<String,Node> map = new HashMap<String, Node>();
		cont.put("bindings", map);
		
		map.put("1", NodeFactory.createLiteral(""+dist, XSDDatatype.XSDdouble));
		map.put("2", NodeFactory.createLiteral(""+lat1, XSDDatatype.XSDdouble));
		map.put("3", NodeFactory.createLiteral(""+long1, XSDDatatype.XSDdouble));
		map.put("4", NodeFactory.createLiteral(""+lat2, XSDDatatype.XSDdouble));
		map.put("5", NodeFactory.createLiteral(""+long2, XSDDatatype.XSDdouble));
		
		List<Context> ret = ihdf.getWrapped().apply(cont);
		return ret;
	}
	
}
