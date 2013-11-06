package org.openimaj.rif.contentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.openimaj.io.FileUtils;
import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.conditions.atomic.RIFFrame;
import org.openimaj.rif.conditions.data.RIFConst;
import org.openimaj.rif.conditions.data.RIFIRIConst;
import org.openimaj.rif.conditions.data.RIFStringConst;
import org.openimaj.rif.conditions.data.RIFTypedConst;
import org.openimaj.rif.rules.RIFGroup;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class SimpleNTriplesImportHandler implements RIFImportHandler {

	@Override
	public RIFRuleSet importToRuleSet(URI loc, RIFRuleSet ruleSet)
			throws SAXException, IOException {
		String fileNameOrUri = loc.toString();
	    Model model = ModelFactory.createDefaultModel();
	    InputStream is = FileManager.get().open(fileNameOrUri);
	    if (is != null) {
	        model.read(is, "N-TRIPLE");
	        
	        ruleSet.addRootGroup(new RIFGroup());
	        
	        ExtendedIterator<Triple> triples = model.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
	        while (triples.hasNext()){
	        	Triple triple = triples.next();
	        	RIFFrame frame = new RIFFrame();
	        	RIFConst<?> subject, predicate, object;
	        	Node sub = triple.getSubject(),
	        		 pred = triple.getPredicate(),
	        		 ob = triple.getObject();
	        	if (sub.isURI()){
	        		subject = new RIFIRIConst();
					try {
						((RIFIRIConst) subject).setData(new URI(sub.getURI()));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	} else if (sub.isLiteral()) {
	        		try {
						subject = new RIFTypedConst(new URI(sub.getLiteralDatatypeURI()));
						((RIFStringConst) subject).setData(sub.getLiteralValue().toString());
					} catch (URISyntaxException | NullPointerException e) {
						subject = new RIFStringConst();
						((RIFStringConst) subject).setData(sub.getLiteralLexicalForm());
					}
	        	} else if (sub.isBlank()) {
	        		subject = new RIFStringConst();
					((RIFStringConst) subject).setData(sub.getBlankNodeLabel());
	        	} else throw new UnsupportedOperationException("NTriples translation: All nodes and predicates must be concrete values.");
	        	frame.setSubject(subject);
	        	
	        	if (pred.isURI()){
	        		predicate = new RIFIRIConst();
					try {
						((RIFIRIConst) predicate).setData(new URI(pred.getURI()));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	} else if (pred.isLiteral()) {
	        		try {
						predicate = new RIFTypedConst(new URI(pred.getLiteralDatatypeURI()));
						((RIFStringConst) predicate).setData(pred.getLiteralValue().toString());
					} catch (URISyntaxException | NullPointerException e) {
						predicate = new RIFStringConst();
						((RIFStringConst) predicate).setData(pred.getLiteralLexicalForm());
					}
	        	} else if (pred.isBlank()) {
	        		predicate = new RIFStringConst();
					((RIFStringConst) predicate).setData(pred.getBlankNodeLabel());
	        	} else throw new UnsupportedOperationException("NTriples translation: All nodes and predicates must be concrete values.");
	        	frame.setPredicate(predicate);
	        	
	        	if (ob.isURI()){
	        		object = new RIFIRIConst();
					try {
						((RIFIRIConst) object).setData(new URI(ob.getURI()));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	} else if (ob.isLiteral()) {
	        		try {
						object = new RIFTypedConst(new URI(ob.getLiteralDatatypeURI()));
						((RIFStringConst) object).setData(ob.getLiteralValue().toString());
					} catch (URISyntaxException | NullPointerException e) {
						object = new RIFStringConst();
						((RIFStringConst) object).setData(ob.getLiteralLexicalForm());
					}
	        	} else if (ob.isBlank()) {
	        		object = new RIFStringConst();
					((RIFStringConst) object).setData(ob.getBlankNodeLabel());
	        	} else throw new UnsupportedOperationException("NTriples translation: All nodes and predicates must be concrete values.");
	        	frame.setObject(object);
	        	
	        	ruleSet.getRootGroup().addSentence(frame);
	        }
	    } else {
	        System.err.println("cannot read " + fileNameOrUri);;
	    }
		return ruleSet;
	}

}
