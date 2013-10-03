package org.openimaj.squall.compile;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Create a {@link CompiledProductionSystem}
 * @param <INPUT> 
 * @param <OUTPUT> 
 * @param <T> The type which the {@link Compiler} can compile from
 *
 */
public interface Compiler<INPUT,OUTPUT,T> {
	
	/**
	 * @param type 
	 * @return produce a {@link CompiledProductionSystem}
	 */
	public CompiledProductionSystem<INPUT,OUTPUT> compile(T type);
}
