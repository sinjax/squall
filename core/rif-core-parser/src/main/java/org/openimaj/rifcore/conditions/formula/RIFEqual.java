package org.openimaj.rifcore.conditions.formula;

import java.util.Iterator;

import org.openimaj.rifcore.conditions.data.RIFDatum;

/**
 * @author david.monks
 *
 */
public class RIFEqual implements RIFFormula, Iterable<RIFDatum> {

	private RIFDatum left;
	private RIFDatum right;
	
	/**
	 * 
	 */
	public RIFEqual(){
		
	}
	
	/**
	 * @param l
	 */
	public void setLeft(RIFDatum l){
		this.left = l;
	}
	
	/**
	 * @param r
	 */
	public void setRight(RIFDatum r){
		this.right = r;
	}
	
	/**
	 * @return
	 */
	public RIFDatum getRight(){
		return this.right;
	}
	
	/**
	 * @return
	 */
	public RIFDatum getLeft(){
		return this.left;
	}

	@Override
	public void addFormula(RIFFormula formula) {
		throw new UnsupportedOperationException("RIF: Cannot encapsulate formuli within a RIF equality statement.");
	}

	@Override
	public Iterator<RIFDatum> iterator() {
		return new Iterator<RIFDatum>(){

			private boolean firstSeen = false;
			private boolean secondSeen = false;
			
			@Override
			public boolean hasNext() {
				return !secondSeen;
			}

			@Override
			public RIFDatum next() {
				if (firstSeen){
					return RIFEqual.this.right;
				} else if (secondSeen){
					throw new IndexOutOfBoundsException("Attempted to access a third value.\nRIF-Core Equal operators only compare two values.");
				}
				return RIFEqual.this.left;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Cannot remove values from a RIF-Core Equal operator.");
			}
			
		};
	}
	
}
