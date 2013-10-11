package org.openimaj.squall.compile.data;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A stream which can be {@link #setup()} and {@link #cleanup()}
 *
 * @param <O>
 */
public interface IStream<O> extends Stream<O>, Initialisable{
	/**
	 * Transform the stream by creating a view that consists of only the items
	 * that match the given {@link Predicate}.
	 *
	 * @param filter
	 *            the predicate
	 * @return a new stream consisting of the matched items from this stream
	 */
	public IStream<O> filter(Predicate<O> filter);

	/**
	 * Transform the stream by creating a new stream that transforms the items
	 * in this stream with the given {@link Function}.
	 *
	 * @param mapper
	 *            the function to apply
	 * @return a new stream with transformed items from this stream
	 */
	public <R> IStream<R> map(Function<O, R> mapper);

	/**
	 * Transform the stream by creating a new stream that transforms the items
	 * in this stream with the given {@link Function}.
	 *
	 * @param mapper
	 *            the function to apply
	 * @return a new stream with transformed items from this stream
	 */
	public <R> IStream<R> map(MultiFunction<O, R> mapper);

	/**
	 * Transform the stream using the given function to transform the items in
	 * this stream.
	 *
	 * @param transform
	 *            the transform function
	 * @return a new stream with transformed items from this stream
	 */
	public <R> IStream<R> transform(Function<Stream<O>, Stream<R>> transform);
}
