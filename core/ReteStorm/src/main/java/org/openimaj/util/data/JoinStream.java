package org.openimaj.util.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 *
 * @param <O>
 */
public class JoinStream<O> extends AbstractStream<O>{

	private static final Logger logger = Logger.getLogger(JoinStream.class);
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
			Stream<O> checkStream = this.streams.get(check);
			boolean streamHasNext = checkStream.hasNext();
			if(!streamHasNext) continue;
			O ret = checkStream.next();
			if(ret != null)
			{
				current = (check + 1) % streams.size();
				logger.debug(String.format("Emitting: %s", ret));
				return ret;
			}
		}
		return null;
	}


	/**
	 * @param split
	 */
	public void addStream(Stream<O> split) {
		this.streams.add(split);
	}
	
	@Override
	public String toString() {
		return this.streams.toString();
	}

}
