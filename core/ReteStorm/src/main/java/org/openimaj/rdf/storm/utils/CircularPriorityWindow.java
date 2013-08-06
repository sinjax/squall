/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import scala.actors.threadpool.Arrays;

/**
 *
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <T>
 */
public class CircularPriorityWindow <T> implements Queue <T> {

	protected PriorityQueue<TimeWrapped> data;
	protected Map<T, Count> queue;
	protected final int capacity;
	protected final long delay;
	protected final TimeUnit unit;
	protected final OverflowHandler<T> continuation;

	/**
	 * @param handler 
	 * @param size
	 * @param delay
	 * @param unit
	 */
	public CircularPriorityWindow(OverflowHandler<T> handler, int size, long delay, TimeUnit unit) {
		this.capacity = size;
		this.unit = unit;
		this.delay = delay;
		this.continuation = handler;
		this.clear();
	}
	
	/**
	 * @return int
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @return long
	 */
	public long getDelay() {
		return delay;
	}
	
//	/**
//	 * @return oldest timestamp
//	 */
//	public long getOldestTimestamp(){
//		this.prune();
//		return data.peek().getTimestamp();
//	}

	@Override
	public void clear() {
		this.data = new PriorityQueue<TimeWrapped>(this.capacity + 1);
		this.queue = new HashMap<T,Count>();
	}

	private void prune() {
		Iterator<TimeWrapped> pruner = new Iterator<TimeWrapped>(){
			TimeWrapped last;
			@Override
			public boolean hasNext() {
				last = data.peek();
				if(last == null) return false;
				return last.getDelay(CircularPriorityWindow.this.unit) < 0;
			}
			@Override
			public TimeWrapped next() {
				return last;
			}
			@Override
			public void remove() {
				data.remove(last);
				T lastUnwrapped = last.getWrapped();
				decrement(lastUnwrapped);
				try {
					((DurationOverflowHandler<T>)continuation).handleDurationOverflow(lastUnwrapped);
				} catch (NullPointerException e) {
					
				}catch (ClassCastException e) {
					
				}
				last = null;
			}
		};

		while (pruner.hasNext())
			pruner.remove();
	}

	@Override
	public boolean contains(Object arg0) {
		return queue.containsKey(arg0) || data.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		boolean contains = true;
		for (Object item : arg0)
			contains &= contains(item);
		return contains;
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public T[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean decrement(T arg0) {
		Count count = queue.get(arg0);
        if (count == null) {
            return false;
        } else {
            count.dec();
            if (count.getCount() == 0) {
                queue.remove(arg0);
            }
        }
        return true;
	}

	private boolean increment(T arg0) {
		Count count = queue.get(arg0);
        if (count == null) {
            queue.put(arg0, new Count(1));
        } else {
            count.inc();
        }
        return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object arg0) {
		try {
			return data.remove(arg0) && decrement(((Wrapped)arg0).getWrapped());
		} catch (ClassCastException e) {}
		try {
			T arg = (T) arg0;
			return data.remove(new Wrapped(arg)) && decrement(arg);
		} catch (ClassCastException e) {}
		return false;

	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removed = true;
		for (Object item : arg0)
			removed &= remove(item);
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean removed = true;
		List<TimeWrapped> removals = new ArrayList<TimeWrapped>();
		for (TimeWrapped item : data){
			boolean toRemove = true;
			for (Object keeper : arg0)
				toRemove &= !item.equals(keeper);
			if (toRemove)
				removals.add(item);
		}
		for (TimeWrapped item : removals)
			removed &= remove(item);
		return removed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection arg0) {
		boolean success = true;
		for (Object item : arg0)
			try {
				success &= add((T)item);
			} catch (ClassCastException e) {
				try {
					success &= add((TimeWrapped)item);
				} catch (ClassCastException ex) {
					success = false;
				}
			}
		return success;
	}

	private boolean add(TimeWrapped arg0){
		prune();
		if (arg0.getWrapped() == null) return false;
		if (data.size() >= capacity){
			try {
				((CapacityOverflowHandler<T>)continuation).handleCapacityOverflow(data.remove().getWrapped());
			} catch (NullPointerException e) {
				data.remove();
			} catch (ClassCastException e) {
				data.remove();
			}
		}
		increment(arg0.getWrapped());
		return data.add(arg0);
	}

	@Override
	public boolean add(T arg0) {
		return add(arg0,(new Date()).getTime(),this.delay,this.unit);
	}
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue (in milliseconds).
	 * @param arg0
	 * 		The datum
	 * @param delay
	 * 		a datum-specific life span, defined in the unit of the Window
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long delay) {
		return add(arg0,(new Date()).getTime(),delay,this.unit);
	}
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue (in milliseconds).
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span, defined in the unit of the Window
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long timestamp, long delay) {
		return add(arg0,timestamp,delay,this.unit);
	}
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue, given in the specified time unit.
	 * @param arg0
	 * 		The datum
	 * @param delay
	 * 		a datum-specific life span
	 * @param unit
	 * 		the time unit that the datum-specific life span is defined in
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long delay, TimeUnit unit) {
		return add(arg0,(new Date()).getTime(),delay,unit);
	}
	
	/**
	 * Adds a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue, given in the specified time unit.
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span
	 * @param unit
	 * 		the time unit that the datum-specific life span is defined in
	 * @return
	 * 		whether the add was successful
	 */
	public boolean add(T arg0, long timestamp, long delay, TimeUnit unit) {
		return add(new TimeWrapped(arg0,timestamp,delay,unit));
	}

	@Override
	public boolean offer(T arg0) {
		try {
			return add(arg0);
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue (in milliseconds).
	 * @param arg0
	 * 		The datum
	 * @param delay
	 * 		a datum-specific life span, defined in the unit of the Window
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long delay) {
		try {
			return add(arg0,delay);
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue (in milliseconds).
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span, defined in the unit of the Window
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long timestamp, long delay) {
		try {
			return add(arg0, timestamp, delay);
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue, given in the specified time unit.
	 * @param arg0
	 * 		The datum
	 * @param delay
	 * 		a datum-specific life span
	 * @param unit
	 * 		the time unit that the datum-specific life span is defined in
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long delay, TimeUnit unit) {
		try {
			return add(arg0, delay, unit);
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * offers a new object of type T to the time-based priority queue, along with the timestamp related to the object and its intended life span in the queue, given in the specified time unit.
	 * @param arg0
	 * 		The datum
	 * @param timestamp
	 * 		an externally defined timestamp to be applied in the system.
	 * @param delay
	 * 		a datum-specific life span
	 * @param unit
	 * 		the time unit that the datum-specific life span is defined in
	 * @return
	 * 		whether the add was successful
	 */
	public boolean offer(T arg0, long timestamp, long delay, TimeUnit unit) {
		try {
			return add(arg0,timestamp,delay,unit);
		} catch (IllegalStateException e) {
			return false;
		}
	}

	@Override
	public T element() {
		prune();
		return data.element().getWrapped();
	}

	@Override
	public T peek() {
		try {
			return element();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public T poll() {
		try {
			return remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public T remove() {
		prune();
		T item = data.remove().getWrapped();
		decrement(item);
		return item;
	}

	@Override
	public Iterator <T> iterator() {
		prune();
		try {
			final List<TimeWrapped> dc = Arrays.asList(data.toArray());
			Collections.sort(dc);
			return new Iterator<T>(){
				List<TimeWrapped> dataclone = dc;
				int index = 0;
				TimeWrapped last;
				@Override
				public boolean hasNext() {
					return index < dataclone.size();
				}
				@Override
				public T next() {
					return (last = dataclone.get(index++)).getWrapped();
				}
				@Override
				public void remove() {
					data.remove(last);
					decrement(last.getWrapped());
				}
			};
		} catch (ClassCastException e) {
			System.out.print(data.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}



	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 * 
	 * @param <E> 
	 */
	public interface OverflowHandler<E> {
		// This is an empty interface serving as an abstract super-interface for both of the distinct (but not disjoint) forms of Overflow Handler.
	}

	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <E>
	 */
	public interface CapacityOverflowHandler<E> extends OverflowHandler<E> {
		
		/**
		 * @param overflow
		 */
		public void handleCapacityOverflow(E overflow);
		
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <E>
	 */
	public interface DurationOverflowHandler<E> extends OverflowHandler<E> {
		
		/**
		 * @param overflow
		 */
		public void handleDurationOverflow(E overflow);
		
	}

	/**
	 * Inner class used to represent a generic wrapper for the generic type T contained by the queue.
	 */
	private class Wrapped {

		private T wrapped;

		public Wrapped (T toWrap) {
			this.wrapped = toWrap;
		}

		public T getWrapped() {
			return this.wrapped;
		}

		@Override
		public int hashCode(){
			return this.wrapped.hashCode();
		}

		@Override
		public boolean equals(Object obj){
			try {
				return this.wrapped.equals(((Wrapped)obj).getWrapped());
			} catch (ClassCastException e) {}
			try {
				return this.wrapped.equals((T)obj);
			} catch (ClassCastException ex) {}
			return false;
		}

	}

	/**
	 * Inner class used to represent a timestamp wrapper for the generic type T contained by the queue.
	 */
	private class TimeWrapped extends Wrapped implements Delayed {

		private long droptime;

		public TimeWrapped (T toWrap, long ts, long delay, TimeUnit delayUnit) {
			super(toWrap);
			droptime = ts + TimeUnit.MILLISECONDS.convert(delay, delayUnit);
		}

		@Override
		public boolean equals(Object obj){
			if (obj.getClass().equals(TimeWrapped.class))
				return getDelay(CircularPriorityWindow.this.unit) == this.getClass().cast(obj).getDelay(CircularPriorityWindow.this.unit)
						&& getWrapped().equals(TimeWrapped.class.cast(obj).getWrapped());
			else
				return super.equals(obj);
		}

		@Override
		public int compareTo(Delayed arg0) {
			return (int) (arg0.getDelay(TimeUnit.MILLISECONDS) - getDelay(TimeUnit.MILLISECONDS));
		}

		@Override
		public long getDelay(TimeUnit arg0) {
			return arg0.convert(droptime - (new Date()).getTime(),TimeUnit.MILLISECONDS);
		}

	}

}