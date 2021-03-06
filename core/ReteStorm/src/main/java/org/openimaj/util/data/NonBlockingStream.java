package org.openimaj.util.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * Given a stream, use {@link GlobalExecutorPool} to call the stream to fill an 
 * {@link ArrayDeque} instance. The next call consumes from the ArrayDeque, returning
 * null if no element is available. In this way this stream never blocks.
 * 
 * This stream's {@link #hasNext()} returns false when both the underlying {@link Deque}
 * is empty and the wrapped stream's {@link Stream#hasNext()} returns false
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <O>
 */
public class NonBlockingStream<O> extends AbstractStream<O>{

	private final class StreamConsumer implements Runnable {
		
		@Override
		public void run() {	
			while(towrap.hasNext()){
				O next = towrap.next();
				synchronized(queue){
					queue.add(next);
				}
			}
		}
	}
	private Stream<O> towrap;
	private Deque<O> queue;
	private Runnable consume;

	
	/**
	 * no background queue probided, so no queue is consumed
	 */
	public NonBlockingStream() {
	}
	/**
	 * @param tw
	 */
	public NonBlockingStream(Stream<O> tw) {
		setWrapped(tw);
	}
	
	
	@Override
	public boolean hasNext() {
		return towrap.hasNext() || !queue.isEmpty();
	}

	@Override
	public O next() {
		O next = null;
		synchronized(queue){			
			next = queue.pollLast();
		}
		if(next == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return next;
	}
	/**
	 * @param reentrantJoin
	 */
	public void setWrapped(Stream<O> tw) {
		this.towrap = tw;
		this.queue = new ArrayDeque<O>();
		this.consume = new StreamConsumer();
		ExecutorService gep = GlobalExecutorPool.getPool();
		gep.execute(consume);
	}

}
