package org.openimaj.rdf.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author david.monks
 *
 */
public class RIFForAll implements RIFSentence {
	
	private Set<RIFVar> universalVars;
	private RIFStatement statement;
	
	/**
	 * 
	 */
	public RIFForAll(){
		this.universalVars = new HashSet<RIFVar>();
	}
	
	/**
	 * @param s
	 */
	public void setStatement(RIFStatement s){
		this.statement = s;
	}
	
	/**
	 * @return
	 */
	public RIFStatement getStatement(){
		return this.statement;
	}
	
	/**
	 * @param var
	 */
	public void addUniversalVar(RIFVar var){
		this.universalVars.add(var);
	}
	
	/**
	 * @return
	 */
	public Iterable<RIFVar> universalVars(){
		return new Iterable<RIFVar>(){
			@Override
			public Iterator<RIFVar> iterator() {
				return RIFForAll.this.universalVars.iterator();
			}
		};
	}

}
