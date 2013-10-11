package org.openimaj.squall.compile.data;

import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Wrap a normal stream as an {@link IStream}. Allow the chaining into {@link IStream} instances
 *
 * @param <O>
 */
public class IStreamWrapper<O> implements IStream<O>{
	
	private Stream<O> wrapped;
	private Initialisable init;
	
	/**
	 * @param wrapped the stream to wrap
	 * @param init how the wrapped stream must be initialised
	 */
	public IStreamWrapper(Stream<O> wrapped, Initialisable init) {
		this.wrapped = wrapped;
		this.init = init;
	}
	
	
	
	@Override
	public IStream<O> filter(Predicate<O> filter) {
		Stream<O> wfilter = wrapped.filter(filter);
		return new IStreamWrapper<>(wfilter, init);
	}

	@Override
	public <R> IStream<R> map(Function<O, R> mapper) {
		Stream<R> wfilter = wrapped.map(mapper);
		return new IStreamWrapper<>(wfilter, init);
	}

	@Override
	public <R> IStream<R> map(MultiFunction<O, R> mapper) {
		Stream<R> wfilter = wrapped.map(mapper);
		return new IStreamWrapper<>(wfilter, init);
	}

	@Override
	public <R> IStream<R> transform(Function<Stream<O>, Stream<R>> transform) {
		Stream<R> wfilter = wrapped.transform(transform);
		return new IStreamWrapper<>(wfilter, init);
	}

	@Override
	public void forEach(Operation<O> op) {
		wrapped.forEach(op);
	}

	@Override
	public void forEach(Operation<O> operation, Predicate<O> stopPredicate) {
		wrapped.forEach(operation,stopPredicate);
	}

	@Override
	public int forEach(Operation<O> operation, int limit) {
		return wrapped.forEach(operation,limit);
	}

	@Override
	public void parallelForEach(Operation<O> op) {
		wrapped.parallelForEach(op);
	}

	@Override
	public void parallelForEach(Operation<O> op, ThreadPoolExecutor pool) {
		wrapped.parallelForEach(op,pool);
	}

	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	@Override
	public O next() {
		return wrapped.next();
	}

	@Override
	public void remove() {
		wrapped.remove();
	}

	@Override
	public Iterator<O> iterator() {
		return wrapped.iterator();
	}

	@Override
	public void setup() {
		this.init.setup();
	}

	@Override
	public void cleanup() {
		this.init.cleanup();
	}
}
