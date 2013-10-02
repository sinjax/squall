package org.openimaj.util.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A {@link SplitStream} wraps a {@link Stream} and maintains a {@link LinkedList}
 * of its elements. Internally all {@link Stream} calls are returned {@link Stream} 
 * instances which use the linked list with different iterators. In this way a 
 * single stream can be split.
 * 
 * Because this {@link Stream} is fundamentally a wrapper, {@link #hasNext()} and {@link #next()}
 * when called directly throw {@link UnsupportedOperationException}
 * @param <T> 
 *
 */
public class SplitStream<T> implements Stream<T>{
	
	private Stream<T> inner;
	private List<ConcurrentLinkedQueue<T>> lists;
	class ConcurrentLinkedQueueStream extends AbstractStream<T>{
		
		private ConcurrentLinkedQueue<T> queue;

		public ConcurrentLinkedQueueStream(ConcurrentLinkedQueue<T> queue) {
			this.queue = queue;
		}

		@Override
		public boolean hasNext() {
			return !queue.isEmpty() || inner.hasNext();
		}

		@Override
		public T next() {
			while(queue.isEmpty()){
				innerNext();
			}
			return queue.poll();
		}
		
	}
	/**
	 * @param inner
	 */
	public SplitStream(Stream<T> inner) {
		this.inner = inner;
		this.lists = Collections.synchronizedList(new ArrayList<ConcurrentLinkedQueue<T>>());
	}
	
	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T next() {
		throw new UnsupportedOperationException();
	}
	private ConcurrentLinkedQueue<T> registerNewQueue() {
		
		ConcurrentLinkedQueue<T> ret = new ConcurrentLinkedQueue<T>();
		this.lists.add(ret);
		return ret;
	}
	
	private T innerNext(){
		T next = this.inner.next();
		for (ConcurrentLinkedQueue<T> queue : this.lists) {
			queue.offer(next);
		}
		return next;
	}
	

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void forEach(Operation<T> op) {
		newCLQS().forEach(op);
	}

	private ConcurrentLinkedQueueStream newCLQS() {
		ConcurrentLinkedQueue<T> queue = registerNewQueue();
		ConcurrentLinkedQueueStream clqs = new ConcurrentLinkedQueueStream(queue);
		return clqs;
	}

	@Override
	public void forEach(Operation<T> operation, Predicate<T> stopPredicate) {
		newCLQS().forEach(operation,stopPredicate);
	}

	@Override
	public int forEach(Operation<T> operation, int limit) {
		return newCLQS().forEach(operation, limit);
	}

	@Override
	public void parallelForEach(Operation<T> op) {
		newCLQS().parallelForEach(op);
	}

	@Override
	public void parallelForEach(Operation<T> op, ThreadPoolExecutor pool) {
		newCLQS().parallelForEach(op,pool);
	}

	@Override
	public Stream<T> filter(Predicate<T> filter) {
		return newCLQS().filter(filter);
	}

	@Override
	public <R> Stream<R> map(Function<T, R> mapper) {
		return newCLQS().map(mapper);
	}

	@Override
	public <R> Stream<R> map(MultiFunction<T, R> mapper) {
		return newCLQS().map(mapper);
	}

	@Override
	public <R> Stream<R> transform(Function<Stream<T>, Stream<R>> transform) {
		return newCLQS().transform(transform);
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		final long start = System.currentTimeMillis();
		final SplitStream<Double> ss = new SplitStream<Double>(new AbstractStream<Double>() {
			Random r = new Random();
			@Override
			public boolean hasNext() {
				return System.currentTimeMillis() - start < 5000;
			}

			@Override
			public Double next() {
				return r.nextDouble();
			}
		});
		Random r = new Random();
		for (int i = 0; i < 3; i++) {
			final double limit = r.nextDouble();
			new Thread(new Runnable(){
				
				@Override
				public void run() {
					final int[] count = new int[1];
					ss
					.filter(new Predicate<Double>() {
						@Override
						public boolean test(Double d) {
							return d < limit;
						}
					})
					.forEach(new Operation<Double>() {
						
						@Override
						public void perform(Double  object) {
							count[0]++;
						}
					});
					System.out.println("Seen: " + count[0] + ", Limit: " + limit);
				}
			}).start();
			
		}
	}
}
