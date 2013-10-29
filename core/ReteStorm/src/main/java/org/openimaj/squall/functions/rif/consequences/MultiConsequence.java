package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.jena.CombinedIVFunction;
import org.openimaj.util.data.Context;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class MultiConsequence extends CombinedIVFunction<Context, Context> {
	
	/**
	 * 
	 */
	public MultiConsequence(){
		super();
	}
	
	/**
	 * @param cons 
	 * 
	 */
	public MultiConsequence(IVFunction<Context,Context> cons){
		super();
		this.addFunction(cons);
	}
	
	/**
	 * @param cons 
	 * 
	 */
	public MultiConsequence(MultiConsequence cons){
		super();
		for (IVFunction<Context,Context> func : cons.functions())
			this.addFunction(func);
	}
	
	/**
	 * @param cons 
	 * 
	 */
	public MultiConsequence(List<IVFunction<Context,Context>> cons){
		super();
		for (IVFunction<Context,Context> func : cons)
			this.addFunction(func);
	}
	
	@Override
	protected List<Context> combine(List<Context> out, List<Context> apply) {
		out.addAll(apply);
		return out;
	}

	@Override
	protected List<Context> initial() {
		return new ArrayList<Context>();
	}

}
