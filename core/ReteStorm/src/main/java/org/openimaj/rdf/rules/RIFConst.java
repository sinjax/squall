package org.openimaj.rdf.rules;

/**
 * @author david.monks
 * @param <T> 
 *
 */
public abstract class RIFConst <T> implements RIFDatum {

	protected T data;
	
	/**
	 * 
	 */
	public RIFConst(){
		
	}
	
	/**
	 * @param data
	 */
	public void setData(T data){
		this.data = data;
	}
	
	/**
	 * @return
	 */
	public T getData(){
		return this.data;
	}
	
}
