package org.openimaj.rdf.storm.utils;

import scala.actors.threadpool.Arrays;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public class DeepHashArray<T> {

	private final T[] arr;
	
	/**
	 * @param a
	 */
	public DeepHashArray(T[] a){
		this.arr = a;
	}
	
	/**
	 * @param index
	 * @param item
	 */
	public void set(int index, T item){
		this.arr[index] = item;
	}
	
	/**
	 * @param index
	 * @return
	 */
	public T get(int index){
		return this.arr[index];
	}
	
	@Override
	public int hashCode() {
		return Arrays.deepHashCode(this.arr);
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DeepHashArray) && obj.hashCode() == this.hashCode();
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.arr);
	}
	
}
