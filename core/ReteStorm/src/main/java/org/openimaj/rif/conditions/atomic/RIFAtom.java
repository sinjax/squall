package org.openimaj.rif.conditions.atomic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.rif.conditions.data.RIFConst;
import org.openimaj.rif.conditions.data.RIFData;
import org.openimaj.rif.conditions.data.RIFDatum;

/**
 * @author david.monks
 *
 */
public class RIFAtom extends RIFAtomic implements Iterable<RIFDatum> {
	
	private RIFConst<?> op;
	private List<RIFDatum> args;
	
	/**
	 * 
	 */
	public RIFAtom(){
		this.args = new ArrayList<RIFDatum>();
	}
	
	/**
	 * @param op
	 */
	public void setOp(RIFConst<?> op){
		this.op = op;
	}
	
	/**
	 * @return
	 */
	public RIFConst<?> getOp(){
		return this.op;
	}
	
	/**
	 * @param arg
	 */
	public void addArg(RIFDatum arg){
		this.args.add(arg);
	}
	
	/**
	 * @param index
	 * @return
	 */
	public RIFData getArg(int index){
		return this.args.get(index);
	}
	
	/**
	 * @return
	 */
	public int getArgsSize(){
		return this.args.size();
	}
	
	@Override
	public Iterator<RIFDatum> iterator(){
		return this.args.iterator();
	}

}
