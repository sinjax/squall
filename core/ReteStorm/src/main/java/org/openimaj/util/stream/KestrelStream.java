package org.openimaj.util.stream;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.lag.kestrel.thrift.Item;
import net.lag.kestrel.thrift.QueueInfo;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.storm.utils.KestrelParsedURI;
import org.openimaj.storm.utils.KestrelUtils;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.spout.UnreliableKestrelThriftSpout;
import backtype.storm.utils.Utils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class KestrelStream<T> extends AbstractStream<T>{

	private class EmitItem {
		public T tuple;

		public EmitItem(T tuple, KestrelSourceId sourceId) {
			this.tuple = tuple;
		}
	}

	private static class KestrelSourceId {
		public KestrelSourceId(int index, long id) {
			this.index = index;
			this.id = id;
		}

		int index;
		long id;

		@Override
		public String toString() {
			return String.format("{client:%s,id:%s}", index, id);
		}
	}

	private static final int HOLD_ITEMS = 1000;
	private static final int TIMEOUT = 100;
	private static final Logger logger = Logger.getLogger(UnreliableKestrelThriftSpout.class);
	private List<KestrelServerSpec> clients;
	private Queue<EmitItem> tuples;
	private String queue;
	private int MAX_ITEMS_PER_QUEUE;
	private List<String> hosts;
	private int port;
	private MultiFunction<byte[], T> transform;

	
	/**
	 * @param host
	 * @param transform
	 */
	public KestrelStream(URI host, MultiFunction<byte[], T> transform) {
		KestrelParsedURI hostQueue = KestrelUtils.parseKestrelURI(host);
		init(hostQueue, transform);
	}
	
	/**
	 * @param serverSpecs
	 *            servers to connect to in a round robin fasion
	 * @param inputQueue
	 *            queue from which to read
	 * @param transform how to read items from the queue
	 */
	public KestrelStream(KestrelParsedURI hostQueue, 
			MultiFunction<byte[], T> transform)
	{
		init(hostQueue, transform);
	}

	private void init(KestrelParsedURI hostQueue, MultiFunction<byte[], T> transform) {
		this.transform = transform;
		this.queue = hostQueue.queue;
		this.port = -1;
		this.hosts = new ArrayList<String>();
		for (final KestrelServerSpec kestrelServerSpec : hostQueue.hosts) {
			this.hosts.add(kestrelServerSpec.host);
			this.port = kestrelServerSpec.port;
		}
		
		this.clients = new ArrayList<KestrelServerSpec>();
		for (final String specs : this.hosts) {
			clients.add(new KestrelServerSpec(specs, this.port));
		}
		MAX_ITEMS_PER_QUEUE = HOLD_ITEMS / this.clients.size();
		this.tuples = new LinkedList<EmitItem>();
	}

	/**
	 * close the kestrel clients
	 */
	public void close() {
		for (final KestrelServerSpec client : this.clients)
		{
			client.close();
		}
	}

	int readTotal = 0;

	private void getSomeMoreTuples() {
		if (this.tuples.size() > HOLD_ITEMS - MAX_ITEMS_PER_QUEUE / 2)
			return;
		int clientIndex = 0;
		for (final KestrelServerSpec clientSpec : this.clients) {
			try {
				final KestrelThriftClient client = clientSpec.getValidClient();
				QueueInfo peek = client.peek(this.queue);
				if(peek.get_items() == 0) continue;
				final List<Item> ret = client
						.get(this.queue, MAX_ITEMS_PER_QUEUE, TIMEOUT, TIMEOUT * MAX_ITEMS_PER_QUEUE);
				readTotal += ret.size();
				logger.debug("Read total: " + readTotal);
				final Set<Long> ids = new HashSet<Long>();
				for (final Item item : ret) {
					final long kestrelId = item.get_id();
					ids.add(kestrelId);
					final List<T> deserialize = this.transform.apply(item.get_data());
					
					for (T t : deserialize) {						
						final EmitItem e = new EmitItem(t, new KestrelSourceId(clientIndex, kestrelId));
						this.tuples.add(e);
					}
					
				}
				client.confirm(this.queue, ids);
			} catch (final TException e) {
			}
			clientIndex++;
		}
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public T next() {
		getSomeMoreTuples();
		if (this.tuples.size() == 0)
		{
			return null;
		}
		final EmitItem poll = this.tuples.poll();
		logger.debug(String.format("Emitting: %s", poll.tuple));
		return poll.tuple;
	}

}
