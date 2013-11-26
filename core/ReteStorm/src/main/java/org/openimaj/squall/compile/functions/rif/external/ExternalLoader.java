package org.openimaj.squall.compile.functions.rif.external;

import org.openimaj.squall.compile.functions.rif.external.geo.GeoInHaversineDistanceProvider;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionRegistry;

public class ExternalLoader {

	public static void loadExternals(){
		ExternalFunctionRegistry.register("http://www.ins.cwi.nl/sib/rif-builtin-predicate/geo-in-haversine-distance", new GeoInHaversineDistanceProvider());
	}

}
