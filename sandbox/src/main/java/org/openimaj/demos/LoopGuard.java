package org.openimaj.demos;

import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

public class LoopGuard<T> extends AbstractStream<T>{
	private Stream<T> strm;
	private boolean beingCalled = false;

	public LoopGuard(Stream<T> strm) {
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
		return strm.next();
	}


}
