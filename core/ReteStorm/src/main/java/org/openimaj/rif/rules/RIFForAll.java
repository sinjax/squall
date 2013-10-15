package org.openimaj.rif.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openimaj.rif.conditions.data.datum.RIFVar;

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
	
	/**
	 * @param varName
	 * @return
	 */
	public boolean containsVar(String varName){
		for (RIFVar var : universalVars())
			if (var.getName().equals(varName)) return true;
		return false;
	}

}
