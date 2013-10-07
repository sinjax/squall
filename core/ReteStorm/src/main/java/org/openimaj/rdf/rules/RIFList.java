package org.openimaj.rdf.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author david.monks
 *
 */
public class RIFList implements RIFData, Iterable<RIFData> {

	private List<RIFData> list;
	
	/**
	 * 
	 */
	public RIFList(){
		this.list = new ArrayList<RIFData>();
	}
	
	/**
	 * @param data
	 */
	public void add(RIFData data){
		this.list.add(data);
	}
	
	/**
	 * @param index
	 * @return
	 */
	public RIFData get(int index){
		return this.list.get(index);
	}
	
	@Override
	public Iterator<RIFData> iterator(){
		return this.list.iterator();
	}
	
}
