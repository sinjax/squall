package org.openimaj.squall.compile.functions.rif.external.geo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFStringConst;
import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestInHaversineDistanceFunction {
	
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
		assertTrue(!ret.isEmpty());
		
		//London - Moscow
		ret = performHaversineTest(1000,places[2][0],places[2][1],places[4][0],places[4][1]);
		assertTrue(ret.isEmpty());
		
//		//Aukland - Moscow
//		ret = performHaversineTest(1000,places[2][0],places[2][1],places[4][0],places[4][1]);
//		assertTrue(ret.isEmpty());
		
		//Bristol - Dover
		ret = performHaversineTest(300,places[11][0],places[11][1],places[12][0],places[12][1]);
		assertTrue(!ret.isEmpty());
	}
	
	/**
	 * 
	 */
	@Test
	public void testInHaversineDistance(){
		
		List<Context> ret = performHaversineTest(1000,1000,1000,1000,1000);
		assertTrue(!ret.isEmpty());
	}

	private List<Context> performHaversineTest(double dist, double lat1, double long1, double lat2, double long2) {
		RIFExternalValue e = new RIFExternalValue();
		RIFAtom a = new RIFAtom();
		e.setVal(a);
		
		RIFStringConst c = new RIFStringConst();
		a.setOp(c);
		
		RIFVar v1 = new RIFVar();
		v1.setName("dist", 1);
		a.addArg(v1);
		RIFVar v2 = new RIFVar();
		v2.setName("lat1", 2);
		a.addArg(v2);
		RIFVar v3 = new RIFVar();
		v3.setName("long1", 3);
		a.addArg(v3);
		RIFVar v4 = new RIFVar();
		v4.setName("lat2", 4);
		a.addArg(v4);
		RIFVar v5 = new RIFVar();
		v5.setName("long2", 5);
		a.addArg(v5);
		
		Context cont = new Context();
		Map<String,Node> map = new HashMap<String, Node>();
		cont.put("bindings", map);
		
		map.put("dist", Node.createLiteral(""+dist, XSDDatatype.XSDdouble));
		map.put("lat1", Node.createLiteral(""+lat1, XSDDatatype.XSDdouble));
		map.put("long1", Node.createLiteral(""+long1, XSDDatatype.XSDdouble));
		map.put("lat2", Node.createLiteral(""+lat2, XSDDatatype.XSDdouble));
		map.put("long2", Node.createLiteral(""+long2, XSDDatatype.XSDdouble));
		
		List<Context> ret = (new GeoInHaversineDistanceProvider()).apply(e).apply(cont);
		return ret;
	}
	
}
