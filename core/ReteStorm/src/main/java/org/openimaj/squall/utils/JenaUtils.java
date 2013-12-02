package org.openimaj.squall.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.openimaj.rdf.storm.utils.JenaStormUtils;

import com.esotericsoftware.kryo.Kryo;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Some helper functions for jena
 *
 */
public class JenaUtils {
	
	/**
	 * @param stream
	 * @return rules found in a stream
	 */
	public static List<Rule> readRules(InputStream stream){
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(new InputStreamReader(stream))));
		return rules;
	}
	
	
	/**
	 * @param inputStream
	 * @return read all the triples from an inputstream
	 */
	public static Collection<Triple> readNTriples(InputStream inputStream) {
		Graph graph = GraphFactory.createGraphMem();;
		RDFDataMgr.read(graph, inputStream, Lang.NTRIPLES);
		return allTriples(graph);
	}


	private static Collection<Triple> allTriples(Graph graph) {
		List<Triple> ret = new ArrayList<Triple>();
		ExtendedIterator<Triple> git = GraphUtil.findAll(graph);
		while(git.hasNext()){
			ret.add(git.next());
		}
		return ret ;
	}
	
	/**
	 * @param inputStream
	 * @return read all the triples from an inputstream
	 */
	public static Collection<Triple> readTurtle(InputStream inputStream) {
		Graph graph = GraphFactory.createGraphMem();;
		RDFDataMgr.read(graph, inputStream, Lang.TURTLE);
		return allTriples(graph);
	}


	/**
	 * @param inputStream
	 * @param lang
	 * @return triples in a language
	 */
	public static Collection<Triple> readTriples(InputStream inputStream, Lang lang) {
		final Collection<Triple> tripleCol = new ArrayList<Triple>();
		Iterator<Triple> iter = createIterator(inputStream, lang);
		while(iter.hasNext()){
			tripleCol.add(iter.next());
		}
		return tripleCol;
	}
	
	/**
	 * @param is
	 * @param lang 
	 * @return an iterator
	 */
	public static Iterator<Triple> createIterator(final InputStream is, final Lang lang){
	        // Create a PipedRDFStream to accept input and a PipedRDFIterator to
	        // consume it
	        // You can optionally supply a buffer size here for the
	        // PipedRDFIterator, see the documentation for details about recommended
	        // buffer sizes
	        PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>(1000000);
	        final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

	        // PipedRDFStream and PipedRDFIterator need to be on different threads
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create a runnable for our parser thread
	        Runnable parser = new Runnable() {

	            @Override
	            public void run() {
	                // Call the parsing process.
	                RDFDataMgr.parse(inputStream, is, "", lang);
	            }
	        };

	        // Start the parser on another thread
	        executor.submit(parser);
	        
	        return iter;
	}
	
	
}
