package org.openimaj.rdf.storm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow.CapacityOverflowHandler;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow.DurationOverflowHandler;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow.OverflowHandler;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <K>
 * @param <V>
 */
public class HashedCircularPriorityWindow<K, V> implements TimedMap<K,V>, SpaceLimitedCollection {
	
	private static final Logger logger = Logger.getLogger(HashedCircularPriorityWindow.class);

	private final Map<K,List<V>> map;
	private final PriorityQueue<TimedMapEntry> queue;
	
	protected final int semanticCapacity;
	protected final int maxCapacity;
	protected final long delay;
	protected final TimeUnit unit;
	protected final OverflowHandler<V> continuation;
	
	/**
	 * @param handler 
	 * @param queryCap 
	 * @param delay 
	 * @param unit 
	 * 
	 */
	public HashedCircularPriorityWindow(OverflowHandler<V> handler, int queryCap, long delay, TimeUnit unit){
		this(handler, queryCap, queryCap*2, delay, unit);
	}
	
	/**
	 * @param handler 
	 * @param queryCap 
	 * @param maxCap 
	 * @param delay 
	 * @param unit 
	 * 
	 */
	public HashedCircularPriorityWindow(OverflowHandler<V> handler, int queryCap, int maxCap, long delay, TimeUnit unit){
		this.map = new HashMap<K, List<V>>();
		this.queue = new PriorityQueue<TimedMapEntry>(maxCap + 1);
		
		this.semanticCapacity = queryCap;
		this.maxCapacity = maxCap;
		this.unit = unit;
		this.delay = delay;
		this.continuation = handler;
	}
	
	private void overflowCapacity(V value){
		try {
			((CapacityOverflowHandler<V>)continuation).handleCapacityOverflow(value);
		} catch (NullPointerException e) {
		}catch (ClassCastException e) {
		}
	}
	
	private void overflowDuration(V value){
		try {
			((DurationOverflowHandler<V>)continuation).handleDurationOverflow(value);
		} catch (NullPointerException e) {
		}catch (ClassCastException e) {
		}
	}
	
	@Override
	public void pruneToCapacity() {
		Iterator<TimedMapEntry> pruner = new Iterator<TimedMapEntry>(){
			TimedMapEntry last;
			@Override
			public boolean hasNext() {
				last = HashedCircularPriorityWindow.this.queue.peek();
				if(last == null) return false;
				return HashedCircularPriorityWindow.this.queue.size() > HashedCircularPriorityWindow.this.maxCapacity;
			}
			@Override
			public TimedMapEntry next() {
				return last;
			}
			@Override
			public void remove() {
					V lastValue = last.getValue();
					K lastKey = last.getKey();
					if(map.get(lastKey) == null){
						System.out.println("This should never ever happen. Ever.");
					}
					logger.debug("Removing from key: " + lastKey + " by prune capacity");
					HashedCircularPriorityWindow.this.queue.remove(last);
					HashedCircularPriorityWindow.this.map.get(lastKey).remove(lastValue);
					if (HashedCircularPriorityWindow.this.map.get(lastKey).isEmpty()){
						logger.debug("Removing the window of key: " + lastKey);
						HashedCircularPriorityWindow.this.map.remove(lastKey);
					}
					
					HashedCircularPriorityWindow.this.overflowCapacity(lastValue);
					
					last = null;
			}
		};
		
		while (pruner.hasNext()){
			pruner.remove();
		}
	}
	
	@Override
	public void pruneToDuration() {
		Iterator<TimedMapEntry> pruner = new Iterator<TimedMapEntry>(){
			TimedMapEntry last;
			@Override
			public boolean hasNext() {
				last = HashedCircularPriorityWindow.this.queue.peek();
				if(last == null) return false;
				return last.getDelay(HashedCircularPriorityWindow.this.unit) < 0;
			}
			@Override
			public TimedMapEntry next() {
				return last;
			}
			@Override
			public void remove() {
				V lastValue = last.getValue();
				K lastKey = last.getKey();
				if(map.get(lastKey) == null){
					System.out.println("This should never ever happen. Ever.");
				}
				logger.debug("Removing from key: " + lastKey + " by prune duration");
				HashedCircularPriorityWindow.this.queue.remove(last);
				HashedCircularPriorityWindow.this.map.get(lastKey).remove(lastValue);
				if (HashedCircularPriorityWindow.this.map.get(lastKey).isEmpty()){
					HashedCircularPriorityWindow.this.map.remove(lastKey);
				}
				
				HashedCircularPriorityWindow.this.overflowDuration(lastValue);
				
				last = null;
			}
		};

		while (pruner.hasNext()){
			pruner.remove();
		}
	}

	@Override
	public int size() {
		this.pruneToDuration();
		return this.queue.size();
	}

	@Override
	public boolean isEmpty() {
		this.pruneToDuration();
		return this.map.isEmpty();
	}

	@Override
	public void clear() {
		this.map.clear();
		this.queue.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		this.pruneToDuration();
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		this.pruneToDuration();
		for (K key : this.map.keySet()){
			if (this.map.get(key).contains(value)){
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	@Override
	public V put(K key, V value) {
		return this.put(key, value, new Date().getTime(), this.delay, this.unit);
	}
	
	@Override
	public V put(K key, V value, long timestamp) {
		return this.put(key, value, timestamp, this.delay, this.unit);
	}

	@Override
	public V put(K key, V value, long timestamp, long delay) {
		return this.put(key, value, timestamp, delay, this.unit);
	}

	@Override
	public V put(K key, V value, long timestamp, long delay, TimeUnit unit) {
		List<V> window = this.map.get(key);
		if (window == null){
			window = new ArrayList<V>();
			this.map.put(key, window);
		}
		logger.debug("Adding the window for key: " + key);
		if(this.map.get(key) == null){
			System.out.println("Again, this shoudl never ever happen");
		}
		boolean windowAdded = window.add(value);
		TimedMapEntry tme = new TimedMapEntry(key, value, timestamp, delay, unit);
		boolean queueAdded = this.queue.add(tme);
		if (windowAdded && queueAdded){
			this.pruneToCapacity();
			return value;
		}
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()){
			this.put(key, m.get(key));
		}
	}
	
	/**
	 * Gets the sub queue of items added to this queue that matches the given key.  Before returning the queue, prunes the queue to be within the given time window.
	 * @param key
	 * 		The key by which to retrieve items.
	 * @return
	 * 		The queue of items that were added with the same key.  Returns null if there are no items with the given key.
	 */
	public List<V> getWindow(K key){
		this.pruneToDuration();
		return this.map.get(key);
	}

	@Override
	public Collection<V> values() {
		this.pruneToDuration();
		
		Collection<V> vals = new HashSet<V>();
		for (K key : this.map.keySet()){
			for (V value : this.map.get(key)){
				vals.add(value);
			}
		}
		return vals;
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		this.pruneToDuration();
		
		Set<java.util.Map.Entry<K, V>> entrySet = new HashSet<java.util.Map.Entry<K, V>>();
		for (java.util.Map.Entry<K, V> entry : this.queue){
			entrySet.add(entry);
		}
		return entrySet;
	}
	
	private class MapEntry implements java.util.Map.Entry<K,V> {

		private final K key;
		private V value;
		
		public MapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V value) {
			return this.value = value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			try {
				return this.value.equals(((MapEntry)obj).getValue());
			} catch (ClassCastException e) {}
			try {
				return this.value.equals((V)obj);
			} catch (ClassCastException ex) {}
			return false;
		}
		
	}

	/**
	 * Inner class used to represent a timestamped MapEntry for the generic type V contained by the queue and its key of type K.
	 */
	private class TimedMapEntry extends MapEntry implements Delayed {

		private long droptime;

		public TimedMapEntry (K key, V toWrap, long ts, long delay, TimeUnit delayUnit) {
			super(key, toWrap);
			droptime = ts + TimeUnit.MILLISECONDS.convert(delay, delayUnit);
		}

		@Override
		public boolean equals(Object obj){
			if (obj.getClass().equals(TimedMapEntry.class))
				return getDelay(HashedCircularPriorityWindow.this.unit) == this.getClass().cast(obj).getDelay(HashedCircularPriorityWindow.this.unit)
						&& getValue().equals(TimedMapEntry.class.cast(obj).getValue());
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
	
	// METHODS THAT DON'T MAKE SENSE IN THIS OBJECT

	@Override
	public V get(Object key) {
		throw new UnsupportedOperationException("Cannot manually get individuals (rather than queues of matching individuals) from the map.");
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Cannot manually delete dedicated windows to map.");
	}

}
