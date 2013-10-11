package org.openimaj.util.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.queue.BoundedPriorityQueue;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * Given a stream, use {@link GlobalExecutorPool} to call the stream to fill an 
 * {@link ArrayDeque} instance. The next call consumes from the ArrayDeque, returning
 * null if no element is available. In this way this stream never blocks
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <O>
 */
public class NonBlockingStream<O> extends AbstractStream<O>{

	private Stream<O> towrap;
	private Deque<O> queue;
	private Runnable consume;

	/**
	 * @param towrap
	 */
	public NonBlockingStream(Stream<O> tw) {
		this.towrap = tw;
		this.queue = new ArrayDeque<O>();
		this.consume = new Runnable(){
			@Override
			public void run() {	
				while(towrap.hasNext()){
					O next = towrap.next();
					synchronized(queue){
						queue.add(next);
					}
				}
			}
		};
		ExecutorService gep = GlobalExecutorPool.getPool();
		gep.execute(consume);
	}
	
	
	@Override
	public boolean hasNext() {
		return towrap.hasNext() || !queue.isEmpty();
	}

	@Override
	public O next() {
		synchronized(queue){			
			return queue.pollLast();
		}
	}

}
