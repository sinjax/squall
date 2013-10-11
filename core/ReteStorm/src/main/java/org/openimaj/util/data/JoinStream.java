package org.openimaj.util.data;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 *
 * @param <O>
 */
public class JoinStream<O> extends AbstractStream<O>{

	private List<Stream<O>> streams;
	private int current;

	/**
	 * @param streams
	 */
	public JoinStream(List<Stream<O>> streams) {
		this.streams = streams;
		current = 0;
	}
	
	
	/**
	 * @param streamArr
	 */
	@SafeVarargs
	public JoinStream(Stream<O>  ... streamArr) {
		this.streams = new ArrayList<Stream<O>>();
		for (Stream<O> stream : streamArr) {
			this.streams.add(stream);
		}
	}


	@Override
	public boolean hasNext() {
		for (Stream<O> s : this.streams) {
			if(s.hasNext()) 
				return true;
		}
		return false;
	}

	@Override
	public O next() {
		int firstcurrent = current;
		for (int i = 0; i < streams.size(); i++) {
			int check = (firstcurrent + i) % streams.size();
			if(!this.streams.get(check).hasNext()) continue;
			O ret = this.streams.get(check).next();
			if(ret != null)
			{
				current = (check + 1) % streams.size();
				return ret;
			}
		}
		return null;
	}

}
