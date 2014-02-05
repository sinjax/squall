package org.openimaj.squall.compile.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.orchestrate.rete.StreamAwareFixedJoinFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestJenaRuleCompilerFunctions {
	
	/**
	 * 
	 */
	@Test
	public void testTripleFilter(){
		List<ClauseEntry> clause = new ArrayList<ClauseEntry>();
		TriplePattern pattern = new TriplePattern(
				NodeFactory.createVariable("d"), 
				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), 
				NodeFactory.createURI("http://example.com/Driver"));
		clause.add(pattern);
		Rule rule = new Rule(clause, clause);
		TripleFilterFunction tff = new TripleFilterFunction(rule, pattern);
		
		Context ctx = new Context();
		Triple trp = new Triple(
				NodeFactory.createURI("http://example.com/Steve"), 
				NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), 
				NodeFactory.createURI("http://example.com/Driver")
		);
		ctx.put("triple", trp);
		List<Context> ret = tff.apply(ctx );
		Context first = ret.get(0);
		
		Map<String,Node> bindings = first.getTyped("bindings"); 
	}
	
	@Test
	public void testTripleJoin(){
		List<ClauseEntry> clause = new ArrayList<ClauseEntry>();
		// Two simple triple patterns
		TriplePattern p1 = new TriplePattern(
				NodeFactory.createVariable("d"), 
				NodeFactory.createURI("p1"), 
				NodeFactory.createURI("o1"));
		clause.add(p1);
		
		TriplePattern p2 = new TriplePattern(
				NodeFactory.createVariable("d"), 
				NodeFactory.createURI("p2"), 
				NodeFactory.createURI("o2"));
		clause.add(p2);
		
		Rule rule = new Rule(clause, clause);
		TripleFilterFunction tf1 = new TripleFilterFunction(rule, p1);
		TripleFilterFunction tf2 = new TripleFilterFunction(rule, p2);
		
		// Join the two filters
		WindowInformation wi = new WindowInformation(1000,30, TimeUnit.SECONDS);
		StreamAwareFixedJoinFunction j = StreamAwareFixedJoinFunction.ruleWrapped(tf1, tf2);
		NamedStream leftStream = new NamedStream("left");
		NamedStream rightStream = new NamedStream("right");
		j.setLeftStream(leftStream.identifier(), wi);
		j.setRightStream(rightStream.identifier(), wi);
		
		// The data (joines once)
		List<Context> data = new ArrayList<Context>();
		Triple trp = new Triple(
				NodeFactory.createURI("A"), 
				NodeFactory.createURI("p1"), 
				NodeFactory.createURI("o1")
		);
		data.add(new Context("triple", trp));
		trp = new Triple(
				NodeFactory.createURI("B"), 
				NodeFactory.createURI("p1"), 
				NodeFactory.createURI("o1")
		);
		data.add(new Context("triple", trp));
		trp = new Triple(
				NodeFactory.createURI("B"), 
				NodeFactory.createURI("p1"), 
				NodeFactory.createURI("o1")
		);
		data.add(new Context("triple", trp));
		trp = new Triple(
				NodeFactory.createURI("B"), 
				NodeFactory.createURI("p2"), 
				NodeFactory.createURI("o2")
		);
		data.add(new Context("triple", trp));
		
		
		
		// A little dummy builder
		
		Stream<Context> s = new CollectionStream<Context>(data);
		SplitStream<Context> ss = new SplitStream<Context>(s);
		j.setup();
		new JoinStream<Context>(
			ss.map(tf1).map(leftStream.getFunction()),
			ss.map(tf2).map(rightStream.getFunction())
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
