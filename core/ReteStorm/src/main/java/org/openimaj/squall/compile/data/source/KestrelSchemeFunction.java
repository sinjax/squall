package org.openimaj.squall.compile.data.source;

import java.io.InputStream;
import java.net.URI;

import org.openimaj.util.function.Function;

public class KestrelSchemeFunction implements Function<URI, InputStream> {

	@Override
	public InputStream apply(URI in) {
		return null;
	}

}
