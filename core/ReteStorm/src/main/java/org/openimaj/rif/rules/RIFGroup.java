package org.openimaj.rif.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author david.monks
 *
 */
public class RIFGroup implements RIFSentence, Iterable<RIFSentence> {

	private Set<RIFSentence> sentences;
	
	/**
	 * 
	 */
	public RIFGroup(){
		this.sentences = new HashSet<RIFSentence>();
	}
	
	/**
	 * @param sentence
	 */
	public void addSentence(RIFSentence sentence){
		this.sentences.add(sentence);
	}
	
	@Override
	public Iterator<RIFSentence> iterator(){
		return this.sentences.iterator();
	}
	
}
