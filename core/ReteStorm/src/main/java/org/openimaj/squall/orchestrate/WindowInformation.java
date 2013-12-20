package org.openimaj.squall.orchestrate;

import java.util.concurrent.TimeUnit;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class WindowInformation {
	private int capacity = 1000;
	private long duration = Long.MAX_VALUE; 
	private TimeUnit unit = TimeUnit.DAYS;
	
	private boolean respectItemLimits = true;
	
	/**
	 * creates a new window definition with the default capacity of 1000 items and the default unlimited duration.
	 * Window will respect the duration specifications of individual triples received.
	 */
	public WindowInformation(){}
	
	/**
	 * creates a new window definition with the specified capacity and unlimited duration.
	 * Window will respect the duration specifications of individual triples received.
	 * @param cap
	 * 		the specified capacity 
	 */
	public WindowInformation(int cap){
		this.setCapacity(cap);
	}
	
	/**
	 * creates a new window definition with unlimited capacity and the specified duration and granularity.
	 * Window will respect the duration specifications of individual triples received.
	 * @param dur
	 * @param u
	 */
	public WindowInformation(long dur, TimeUnit u){
		this.setCapacity(Integer.MAX_VALUE);
		this.setDuration(dur, u);
	}
	
	/**
	 * creates a new window definition with the specified capacity and the specified duration and granularity.
	 * Window will respect the duration specifications of individual triples received.
	 * @param cap 
	 * @param dur
	 * @param u
	 */
	public WindowInformation(int cap, long dur, TimeUnit u){
		this.setCapacity(cap);
		this.setDuration(dur, u);
	}
	
	/**
	 * creates a new window definition with the default capacity of 1000 items and the default unlimited duration.
	 * Window will override the duration specifications of individual triples received if the value of override is true.
	 * @param override 
	 */
	public WindowInformation(boolean override){
		this.respectItemLimits = !override;
	}
	
	/**
	 * creates a new window definition with the specified capacity and unlimited duration.
	 * Window will override the duration specifications of individual triples received if the value of override is true.
	 * @param override 
	 * @param cap
	 * 		the specified capacity 
	 */
	public WindowInformation(boolean override, int cap){
		this.setCapacity(cap);
		this.respectItemLimits = !override;
	}
	
	/**
	 * creates a new window definition with unlimited capacity and the specified duration and granularity.
	 * Window will override the duration specifications of individual triples received if the value of override is true.
	 * @param override 
	 * @param dur
	 * @param u
	 */
	public WindowInformation(boolean override, long dur, TimeUnit u){
		this.setCapacity(Integer.MAX_VALUE);
		this.setDuration(dur, u);
		this.respectItemLimits = !override;
	}
	
	/**
	 * creates a new window definition with the specified capacity and the specified duration and granularity.
	 * Window will override the duration specifications of individual triples received if the value of override is true.
	 * @param override 
	 * @param cap 
	 * @param dur
	 * @param u
	 */
	public WindowInformation(boolean override, int cap, long dur, TimeUnit u){
		this.setCapacity(cap);
		this.setDuration(dur, u);
		this.respectItemLimits = !override;
	}
	
	/**
	 * sets the capacity value of the window definition to the specified capacity.
	 * @param cap
	 * 		the specified capacity
	 */
	public void setCapacity(int cap){
		this.capacity = cap;
	}
	
	/**
	 * @return
	 * 		The capacity of windows with this definition
	 */
	public int getCapacity(){
		return this.capacity;
	}
	
	/**
	 * changes the granularity of the window duration to the specified unit, also converting the existing duration to the new granularity.  The new duration value is returned.
	 * @param u
	 * 		The new duration granularity
	 * @return
	 * 		The new duration value
	 */
	public long changeGranularity(TimeUnit u){
		this.duration = u.convert(this.duration, this.unit);
		this.unit = u;
		return this.duration;
	}
	
	/**
	 * @return
	 * 		The duration granularity of windows with this definition
	 */
	public TimeUnit getGranularity(){
		return this.unit;
	}
	
	/**
	 * sets the default maximum age of items in windows with this definition, as well as the granularity of that age
	 * @param dur 
	 * @param u 
	 */
	public void setDuration(long dur, TimeUnit u){
		this.duration = dur;
		this.unit = u;
	}
	
	/**
	 * @return
	 * 		The duration of windows with this definition
	 */
	public long getDuration(){
		return this.duration;
	}
	
	/**
	 * Window will override the duration specifications of individual triples received if the value of override is true.
	 * @param override
	 */
	public void setOverride(boolean override){
		this.respectItemLimits = !override;
	}
	
	/**
	 * Window will override the duration specifications of individual triples received if the value of override is true.
	 * @return
	 * 		The value of override
	 */
	public boolean isOverriding(){
		return !this.respectItemLimits;
	}
}
