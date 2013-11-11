package org.openimaj.squall.functions.rif.sources;

import java.net.URI;

import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class ISourceFactory {
	
	/**
	 * @param location
	 * @return
	 */
	public abstract ISource<Stream<Context>> createSource(URI location);

}
