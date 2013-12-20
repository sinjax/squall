package org.openimaj.squall.compile.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.orchestrate.greedy.FixedJoinFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestJenaRuleCompilerFunctions {
	
	/**
	 * 
	 */
	@Test
	public void testTripleFilter(){
		TriplePattern pattern = new TriplePattern(
				Node.createVariable("d"), 
				Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), 
				Node.createURI("http://example.com/Driver"));
		TripleFilterFunction tff = new TripleFilterFunction(pattern );
		
		Context ctx = new Context();
		Triple trp = new Triple(
			Node.createURI("http://example.com/Steve"), 
			Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), 
			Node.createURI("http://example.com/Driver")
		);
		ctx.put("triple", trp);
		List<Context> ret = tff.apply(ctx );
		Context first = ret.get(0);
		
		Map<String,Node> bindings = first.getTyped("bindings"); 
	}
	
	@Test
	public void testTripleJoin(){
		// Two simple triple patterns
		TriplePattern p1 = new TriplePattern(
				Node.createVariable("d"), 
				Node.createURI("p1"), 
				Node.createURI("o1"));
		TripleFilterFunction tf1 = new TripleFilterFunction(p1);
		
		TriplePattern p2 = new TriplePattern(
				Node.createVariable("d"), 
				Node.createURI("p2"), 
				Node.createURI("o2"));
		TripleFilterFunction tf2 = new TripleFilterFunction(p2);
		
		// Join the two filters
		WindowInformation wi = new WindowInformation(1000,30, TimeUnit.SECONDS);
		FixedJoinFunction j = new FixedJoinFunction(tf1, tf2, wi);
		
		// The data (joines once)
		List<Context> data = new ArrayList<Context>();
		Triple trp = new Triple(
			Node.createURI("A"), 
			Node.createURI("p1"), 
			Node.createURI("o1")
		);
		data.add(new Context("triple", trp));
		trp = new Triple(
			Node.createURI("B"), 
			Node.createURI("p1"), 
			Node.createURI("o1")
		);
		data.add(new Context("triple", trp));
		trp = new Triple(
			Node.createURI("B"), 
			Node.createURI("p1"), 
			Node.createURI("o1")
		);
		data.add(new Context("triple", trp));
		trp = new Triple(
			Node.createURI("B"), 
			Node.createURI("p2"), 
			Node.createURI("o2")
		);
		data.add(new Context("triple", trp));
		
		
		
		// A little dummy builder
		
		Stream<Context> s = new CollectionStream<Context>(data);
		SplitStream<Context> ss = new SplitStream<Context>(s);
		j.setup();
		new JoinStream<Context>(
			ss.map(tf1).map(new NamedStream("left").getFunction()),
			ss.map(tf2).map(new NamedStream("right").getFunction())
		)
		.map(j)
		.forEach(new Operation<Context>() {
			
			@Override
			public void perform(Context object) {
				System.out.println(object);
			}
		});
		
	}
}
