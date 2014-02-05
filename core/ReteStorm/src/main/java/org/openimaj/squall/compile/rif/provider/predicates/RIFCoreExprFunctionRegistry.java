package org.openimaj.squall.compile.rif.provider.predicates;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreExprFunctionRegistry extends RIFExprFunctionRegistry {
	
	private static RIFCoreExprFunctionRegistry REG;
	
	/**
	 * @return
	 */
	public static RIFCoreExprFunctionRegistry getRegistry(){
		if (REG == null){
			REG = new RIFCoreExprFunctionRegistry();
		}
		return REG;
	}
	
	private RIFCoreExprFunctionRegistry(){
		
	}

}
