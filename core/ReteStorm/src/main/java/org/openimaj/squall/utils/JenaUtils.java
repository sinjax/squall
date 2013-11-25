package org.openimaj.squall.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

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
		final Collection<Triple> tripleCol = new ArrayList<Triple>();
		RiotReader.createParserNTriples(inputStream, new Sink<Triple>() {
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void send(Triple item) {
				tripleCol.add(item);
			}
			
			@Override
			public void flush() {
			}
		}).parse();
		return tripleCol;
	}
	
	/**
	 * @param inputStream
	 * @return read all the triples from an inputstream
	 */
	public static Collection<Triple> readTurtle(InputStream inputStream) {
		final Collection<Triple> tripleCol = new ArrayList<Triple>();
		RiotReader.createParserTurtle(inputStream, null,new Sink<Triple>() {
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void send(Triple item) {
				tripleCol.add(item);
			}
			
			@Override
			public void flush() {
			}
		}).parse();
		return tripleCol;
	}
}
