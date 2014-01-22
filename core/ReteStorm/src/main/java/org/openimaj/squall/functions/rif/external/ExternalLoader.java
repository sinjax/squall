package org.openimaj.squall.functions.rif.external;

import org.openimaj.squall.compile.rif.provider.ExternalFunctionRegistry;
import org.openimaj.squall.functions.rif.external.geo.GeoInHaversineDistanceProvider;

public class ExternalLoader {

	public static void loadExternals(){
		ExternalFunctionRegistry.register("http://www.ins.cwi.nl/sib/rif-builtin-predicate/geo-in-haversine-distance", new GeoInHaversineDistanceProvider());
	}

}
