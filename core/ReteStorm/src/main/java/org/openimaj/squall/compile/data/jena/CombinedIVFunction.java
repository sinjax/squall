package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;

/**
 * A {@link CombinedIVFunction} performs all functions on the data.
 * Implementations know how to make an initial, empty output of 
 * a function and further know how to combine the output of 
 * multiple functions in a pairwise manner
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <A> 
 * @param <B> 
 *
 */
public abstract class CombinedIVFunction<A,B> implements IVFunction<A,B> {

	private List<IVFunction<A, B>> functions;

	/**
	 */
	public CombinedIVFunction() {
		this.functions = new ArrayList<IVFunction<A,B>>();
	}
	
	/**
	 * @param func add a function to apply
	 */
	public void addFunction(IVFunction<A,B> func){
		this.functions.add(func);
	}
	
	@Override
	public List<B> apply(A in) {
		List<B> out = initial();
		for (IVFunction<A,B> func: this.functions) {
			out = combine(out,func.apply(in));
		}
		return out;
	}

	protected abstract List<B> combine(List<B> out, List<B> apply) ;

	protected abstract List<B> initial() ;
	
	@Override
	public List<String> variables() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String anonimised() {
		String out = "";
		for (IVFunction<A, B> func : this.functions) {
			out += func.anonimised() + " ";
		}
		return out.trim();
	}
	
	@Override
	public String anonimised(Map<String, Integer> varmap) {
		String out = "";
		for (IVFunction<A, B> func : this.functions) {
			out += func.anonimised(varmap) + " ";
		}
		return out;
	}
	
	@Override
	public void setup() {
		for (IVFunction<A, B> func : this.functions) {
			func.setup();
		}
	}
	
	@Override
	public void cleanup() {
		for (IVFunction<A, B> func : this.functions) {
			func.cleanup();
		}
	}
	
	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Implement Variable Mapping
		
	}

}
