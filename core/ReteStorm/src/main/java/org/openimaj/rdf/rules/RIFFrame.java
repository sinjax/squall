package org.openimaj.rdf.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * @author david.monks
 *
 */
public class RIFFrame extends RIFAtomic {
	
	private RIFData subject;
	private List<PredObPair> predObs;
	
	/**
	 * 
	 */
	public RIFFrame(){
		this.predObs = new ArrayList<PredObPair>();
		this.predObs.add(new PredObPair());
	}
	
	private RIFFrame(RIFData s){
		this();
		this.subject = s;
	}
	
	private RIFFrame(RIFData s, List<PredObPair> pol){
		this();
		this.subject = s;
		this.predObs = pol;
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
		this.predObs.get(this.predObs.size() - 1).setPredicate(pred);
	}
	
	/**
	 * @param obj
	 */
	public void setObject(RIFData obj){
		this.predObs.get(this.predObs.size() - 1).setObject(obj);
	}
	
	/**
	 * 
	 */
	public void newPredObPair(){
		this.predObs.add(new PredObPair());
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
	public int getPredicateObjectPairCount(){
		return this.predObs.size();
	}
	
	/**
	 * @param index
	 * @return
	 */
	public RIFData getPredicate(int index){
		return this.predObs.get(index).getPredicate();
	}
	
	/**
	 * @return
	 */
	public RIFData getObject(int index){
		return this.predObs.get(index).getObject();
	}
	
	/**
	 * @return
	 */
	public RIFData getPredicate(){
		return this.getPredicate(this.getPredicateObjectPairCount() - 1);
	}
	
	/**
	 * @return
	 */
	public RIFData getObject(){
		return this.getObject(this.getPredicateObjectPairCount() - 1);
	}
	
	public RIFFrame clone(){
		return new RIFFrame(this.subject, this.predObs);
	}
	
	/**
	 * @return
	 */
	public RIFFrame cloneSubOnly(){
		return new RIFFrame(this.subject);
	}
	
	private static class PredObPair {
		
		private RIFData predicate;
		private RIFData object;
		
		public PredObPair(){
			
		}
		
		public void setPredicate(RIFData data){
			this.predicate = data;
		}
		
		public void setObject(RIFData data){
			this.object = data;
		}
		
		public RIFData getPredicate(){
			return this.predicate;
		}
		
		public RIFData getObject(){
			return this.object;
		}
		
	}
	
}
