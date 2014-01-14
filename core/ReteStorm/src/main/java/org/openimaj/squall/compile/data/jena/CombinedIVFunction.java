package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.Iterator;
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
@SuppressWarnings("serial")
public abstract class CombinedIVFunction<A,B> extends IVFunction<A,B> {

	private List<IVFunction<A, B>> functions;

	/**
	 */
	public CombinedIVFunction() {
		super();
		this.functions = new ArrayList<IVFunction<A,B>>();
	}
	
	/**
	 * @param func add a function to apply
	 */
	public void addFunction(IVFunction<A,B> func){
		this.functions.add(func);
	}
	
	protected Iterable<IVFunction<A,B>> functions(){
		return new Iterable<IVFunction<A,B>>(){
			@Override
			public Iterator<IVFunction<A,B>> iterator() {
				return CombinedIVFunction.this.functions.iterator();
			}
			
		};
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
	public String identifier() {
		StringBuilder out = new StringBuilder("Combined:");
		for (int i = 0; i < this.functions.size(); i++) {
			out.append("\n")
			   .append(this.functions.get(i).identifier());
		}
		return out.toString();
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		StringBuilder out = new StringBuilder("Combined:");
		for (int i = 0; i < this.functions.size(); i++) {
			out.append("\n")
			   .append(this.functions.get(i).identifier(varmap));
		}
		return out.toString();
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
	public String toString() {
		return this.functions.toString();
	}

}
