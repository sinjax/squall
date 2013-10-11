package org.openimaj.rif.conditions.data.datum;

import java.net.URI;

/**
 * @author david.monks
 * @param <T> 
 *
 */
public abstract class RIFConst <T> implements RIFDatum {

	public static final String datatype = "http://www.w3.org/2001/XMLSchema#thing";
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
	
	/**
	 * @return
	 */
	public String getDatatype(){
		return datatype;
	}
	
}
