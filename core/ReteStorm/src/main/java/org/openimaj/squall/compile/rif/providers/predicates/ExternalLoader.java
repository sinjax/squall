package org.openimaj.squall.compile.rif.providers.predicates;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class ExternalLoader {

	/**
	 * 
	 */
	public static void loadExternals(){
		RIFExternalFunctionRegistry.register(
				"http://www.ins.cwi.nl/sib/rif-builtin-function/geo-haversine-distance",
				new GeoHaversineDistanceProvider(RIFCoreExprFunctionRegistry.getRegistry())
		);
		RIFExternalFunctionRegistry.register(
				"http://www.w3.org/2007/rif-builtin-predicate#numeric-greater-than",
				new NumericGreaterThanProvider(RIFCoreExprFunctionRegistry.getRegistry())
				);
		RIFExternalFunctionRegistry.register(
				"http://www.w3.org/2007/rif-builtin-predicate#literal-not-equal",
				new LiteralNotEqualProvider(RIFCoreExprFunctionRegistry.getRegistry())
				);
	}

}
