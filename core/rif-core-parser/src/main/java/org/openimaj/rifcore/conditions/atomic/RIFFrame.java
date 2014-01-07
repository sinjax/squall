package org.openimaj.rifcore.conditions.atomic;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rifcore.conditions.data.RIFData;
import org.openimaj.rifcore.conditions.data.RIFDatum;

/**
 * @author david.monks
 *
 */
public class RIFFrame extends RIFAtomic {
	
	private RIFDatum subject;
	private List<PredObPair> predObs;
	
	/**
	 * 
	 */
	public RIFFrame(){
		this.predObs = new ArrayList<PredObPair>();
		this.predObs.add(new PredObPair());
	}
	
	private RIFFrame(RIFDatum s){
		this();
		this.subject = s;
	}
	
	private RIFFrame(RIFDatum s, List<PredObPair> pol){
		this();
		this.subject = s;
		this.predObs = pol;
	}

	/**
	 * @param sub
	 */
	public void setSubject(RIFDatum sub){
		this.subject = sub;
	}
	
	/**
	 * @param pred
	 */
	public void setPredicate(RIFDatum pred){
		this.predObs.get(this.predObs.size() - 1).setPredicate(pred);
	}
	
	/**
	 * @param obj
	 */
	public void setObject(RIFDatum obj){
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
	public RIFDatum getSubject(){
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
	public RIFDatum getPredicate(int index){
		return this.predObs.get(index).getPredicate();
	}
	
	/**
	 * @return
	 */
	public RIFDatum getObject(int index){
		return this.predObs.get(index).getObject();
	}
	
	/**
	 * @return
	 */
	public RIFDatum getPredicate(){
		return this.getPredicate(this.getPredicateObjectPairCount() - 1);
	}
	
	/**
	 * @return
	 */
	public RIFDatum getObject(){
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
		
		private RIFDatum predicate;
		private RIFDatum object;
		
		public PredObPair(){
			
		}
		
		public void setPredicate(RIFDatum data){
			this.predicate = data;
		}
		
		public void setObject(RIFDatum data){
			this.object = data;
		}
		
		public RIFDatum getPredicate(){
			return this.predicate;
		}
		
		public RIFDatum getObject(){
			return this.object;
		}
		
	}
	
}
