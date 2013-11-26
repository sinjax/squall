package org.openimaj.squall.orchestrate;

import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WrappedContextISource implements ISource<Stream<Context>>{

	private ISource<Stream<Context>> strm;
	private NamedNode<ISource<Stream<Context>>> nn;
	
	
	/**
	 * @param strm
	 * @param nn
	 */
	public WrappedContextISource(ISource<Stream<Context>> strm, NamedNode<ISource<Stream<Context>>> nn) {
		this.strm = strm;
		this.nn = nn;
	}

	@Override
	public Stream<Context> apply(Stream<Context> in) {
		return apply();
	}
	
	@Override
	public Stream<Context> apply() {
		
		return strm.apply().map(
			new Function<Context, Context>() {
				

				@Override
				public Context apply(Context in) {
					nn.addName(in);
					return in;
				}
			}		
		);
	}
	
	@Override
	public void setup() {
		strm.setup();
	}
	
	@Override
	public void cleanup() {
		strm.cleanup();
	}
	
	@Override
	public String toString() {
		return this.strm.toString();
	}
	
}