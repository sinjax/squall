package org.openimaj.rif.conditions.formula;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openimaj.rif.conditions.data.RIFVar;

/**
 * @author david.monks
 *
 */
public class RIFExists implements RIFFormula {
	
	private Set<RIFVar> existentialVars;
	private RIFFormula formula;
	
	/**
	 * 
	 */
	public RIFExists(){
		this.existentialVars = new HashSet<RIFVar>();
	}
	
	@Override
	public void addFormula(RIFFormula f){
		this.formula = f;
	}
	
	/**
	 * @return
	 */
	public RIFFormula getFormula(){
		return this.formula;
	}
	
	/**
	 * @param var
	 */
	public void addExistentialVar(RIFVar var){
		this.existentialVars.add(var);
	}
	
	/**
	 * @return
	 */
	public Iterable<RIFVar> existentialVars(){
		return new Iterable<RIFVar>(){
			@Override
			public Iterator<RIFVar> iterator() {
				return RIFExists.this.existentialVars.iterator();
			}
		};
	}
	
	/**
	 * @param varName
	 * @return
	 */
	public boolean containsExistentialVar(String varName){
		for (RIFVar var : existentialVars())
			if (var.getNode() != null && var.getNode().getName().equals(varName)) return true;
		return false;
	}

	/**
	 * @param varName
	 * @return
	 */
	public RIFVar getExistentialVar(String varName) {
		for (RIFVar var : existentialVars())
			if (var.getNode().getName().equals(varName)) return var;
		return null;
	}

}
