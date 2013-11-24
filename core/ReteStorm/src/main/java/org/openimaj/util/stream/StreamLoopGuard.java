package org.openimaj.util.stream;


/**
 * Wrapping another stream, this stream guarantees that 
 * when a loop occurs (i.e. this function ends up calling itself)
 * then {@link #hasNext()} returns false and {@link #next()} returns
 * null
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class StreamLoopGuard<T> extends AbstractStream<T>{
	private Stream<T> strm;
	private boolean beingCalled = false;

	/**
	 * @param strm
	 */
	public StreamLoopGuard(Stream<T> strm) {
		this.strm = strm;
	}
	@Override
	public boolean hasNext() {
		boolean toRet = false;
		if(!this.beingCalled){
			this.beingCalled  = true;
			toRet = this.strm.hasNext();
			this.beingCalled = false;
		}
		
		return toRet;
	}

	@Override
	public T next() {
//		T toRet = null;
//		if(!this.beingCalled){
//			this.beingCalled  = true;
//			toRet = this.strm.next();
//			this.beingCalled = false;
//		}
		return this.strm.next();
	}


}
