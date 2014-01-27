package org.openimaj.squall.functions.rif.external;

import org.openimaj.squall.compile.rif.provider.LiteralNotEqualProvider;
import org.openimaj.squall.compile.rif.provider.NumericGreaterThanProvider;
import org.openimaj.squall.compile.rif.provider.RIFCoreExprFunctionRegistry;
import org.openimaj.squall.compile.rif.provider.RIFExternalFunctionRegistry;
import org.openimaj.squall.functions.rif.external.geo.GeoInHaversineDistanceProvider;

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
				"http://www.ins.cwi.nl/sib/rif-builtin-predicate/geo-in-haversine-distance",
				new GeoInHaversineDistanceProvider(RIFCoreExprFunctionRegistry.getRegistry())
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
