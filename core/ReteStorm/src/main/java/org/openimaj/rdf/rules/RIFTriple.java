package org.openimaj.rdf.rules;

/**
 * @author david.monks
 *
 */
public class RIFTriple extends RIFAtomic {
	
	private RIFData subject;
	private RIFData predicate;
	private RIFData object;
	
	/**
	 * 
	 */
	public RIFTriple(){
		
	}
	
	private RIFTriple(RIFData s, RIFData p, RIFData o){
		this.subject = s;
		this.predicate = p;
		this.object = o;
	}

	/**
	 * @param sub
	 */
	public void setSubject(RIFData sub){
		this.subject = sub;
	}
	
	/**
	 * @param pred
	 */
	public void setPredicate(RIFData pred){
		this.predicate = pred;
	}
	
	/**
	 * @param obj
	 */
	public void setObject(RIFData obj){
		this.object = obj;
	}
	
	/**
	 * @return
	 */
	public RIFData getSubject(){
		return this.subject;
	}
	
	/**
	 * @return
	 */
	public RIFData getPredicate(){
		return this.predicate;
	}
	
	/**
	 * @return
	 */
	public RIFData getObject(){
		return this.object;
	}
	
	public RIFTriple clone(){
		return new RIFTriple(this.subject, this.predicate, this.object);
	}
	
}
