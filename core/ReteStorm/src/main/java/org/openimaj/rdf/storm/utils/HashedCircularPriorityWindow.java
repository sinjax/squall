package org.openimaj.rdf.storm.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.utils.CircularPriorityWindow.CapacityOverflowHandler;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow.OverflowHandler;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <K>
 * @param <V>
 */
public class HashedCircularPriorityWindow<K, V> implements TimedMap<K,V> {

	private final Map<K,CircularPriorityWindow<V>> map;
	
	protected final int semanticCapacity;
	protected final int maxCapacity;
	protected final long delay;
	protected final TimeUnit unit;
	protected final OverflowHandler<V> continuation;
	
	protected int size;
	
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
		this.map = new HashMap<K, CircularPriorityWindow<V>>();
		
		this.semanticCapacity = queryCap;
		this.maxCapacity = maxCap;
		this.unit = unit;
		this.delay = delay;
		this.continuation = handler;
		
		this.size = 0;
	}

//	private int capPrune(){
//		int capPruned = 0;
//		while (this.size > this.semanticCapacity) {
//			K soonestKey = null;
//			for (K k : this.map.keySet()){
//				if (soonestKey == null)
//					soonestKey = k;
//				if (this.map.get(soonestKey).getNextExpiry(TimeUnit.MILLISECONDS)
//							> this.map.get(k).getNextExpiry(TimeUnit.MILLISECONDS)){
//					soonestKey = k;
//				}
//			}
//			if (soonestKey != null){
//				V removed = this.map.get(soonestKey).removeNextToExpire();
//				capPruned++;
//				try {
//					((CapacityOverflowHandler<V>)continuation).handleCapacityOverflow(removed);
//				} catch (NullPointerException | ClassCastException e) {}
//				if (this.map.get(soonestKey).isEmpty()){
//					this.map.remove(soonestKey);
//				}
//			}
//		}
//		return capPruned;
//	}
	
	@Override
	public int prune() {
		int pruned = 0;
		for (K key : this.map.keySet()){
			pruned += this.prune(key);
		}
//		if (this.size > this.semanticCapacity){
//			int capPruned = this.capPrune();
//			pruned += capPruned;
//			this.size -= capPruned;
//		}
		return pruned;
	}
	
	private int prune(K key){
		if (this.map.get(key) == null) return 0;
		
		int pruned = this.map.get(key).prune();
		if (this.map.get(key).isEmpty())
			this.map.remove(key);
		this.size -= pruned;
		return pruned;
	}

	@Override
	public int size() {
		this.prune();
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		this.prune();
		return this.map.isEmpty();
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		for (K key : this.map.keySet()){
			this.prune(key);
			if (this.map.containsKey(key) && this.map.get(key).contains(value)){
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
		CircularPriorityWindow<V> window = this.map.get(key);
		if (window == null){
			window = new CircularPriorityWindow<V>(this.continuation, this.semanticCapacity, this.delay, this.unit);
			this.map.put(key, window);
		}
		if (window.offer(value, timestamp, delay, unit)){
			this.size++;
//			if (this.size > this.maxCapacity){
//				int capPruned = this.capPrune();
//				this.size -= capPruned;
//			}
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
	public Queue<V> getWindow(K key){
		this.prune(key);
		CircularPriorityWindow<V> circularPriorityWindow = this.map.get(key);
		return circularPriorityWindow;
	}

	@Override
	public Collection<V> values() {
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
		Set<java.util.Map.Entry<K, V>> entrySet = new HashSet<java.util.Map.Entry<K, V>>();
		for (K key : this.map.keySet()){
			for (V value : this.map.get(key)){
				entrySet.add(new MapEntry(key, value));
			}
		}
		return entrySet;
	}
	
	private final class MapEntry implements java.util.Map.Entry<K,V> {

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
