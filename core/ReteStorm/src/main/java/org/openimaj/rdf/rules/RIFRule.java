package org.openimaj.rdf.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author david.monks
 *
 */
public class RIFRule implements RIFStatement, RIFSentence {
	
	private Set<RIFAtomic> head;
	private RIFFormula body;
	
	/**
	 * 
	 */
	public RIFRule(){
		super();
		this.head = new HashSet<RIFAtomic>();
	}
	
	/**
	 * @param atomic
	 */
	public void addAtomicToHead(RIFAtomic atomic){
		this.head.add(atomic);
	}
	
	/**
	 * @return
	 */
	public Iterable<RIFAtomic> head(){
		return new Iterable<RIFAtomic>(){
			@Override
			public Iterator<RIFAtomic> iterator() {
				return RIFRule.this.head.iterator();
			}
		};
	}
	
	/**
	 * @param formula
	 */
	public void setBody(RIFFormula formula){
		this.body = formula;
	}
	
	/**
	 * @return
	 */
	public RIFFormula getBody(){
		return this.body;
	}

}
