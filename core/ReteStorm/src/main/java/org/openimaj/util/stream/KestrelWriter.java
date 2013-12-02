package org.openimaj.util.stream;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.storm.utils.KestrelParsedURI;
import org.openimaj.storm.utils.KestrelUtils;
import org.openimaj.util.pair.IndependentPair;

import backtype.storm.spout.KestrelThriftClient;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelWriter implements IOperation<byte[]>{

	
	protected final static Logger logger = Logger.getLogger(KestrelWriter.class);

	private String queue;
	private List<KestrelThriftClient> clients;

	private List<ByteBuffer> cache;

	private int cacheSizeLimit;

	private URI host;

	/**
	 * @param host the kestrel host
	 * @throws IOException
	 */
	public KestrelWriter(String host) throws URISyntaxException {
		this.host = new URI(host);
	}
	
	/**
	 * @param host the kestrel host
	 * @throws IOException
	 */
	public KestrelWriter(URI host) {
		this.host = host;
	}
	

	private void flushTripleCache() {
		try {
			KestrelThriftClient nextClient = this.getNextClient();
			nextClient.put(queue, this.cache, 0);
			logger.debug(String.format("Flushing %s Cache, size: %d",queue,this.cache.size()));
			
		} catch (TException e) {
			throw new RuntimeException(e);
		}
		this.cache.clear();
	}




	int currentIndex = 0;

	/**
	 * @return the next {@link KestrelThriftClient} instance ready to be written
	 *         to
	 */
	public KestrelThriftClient getNextClient() {
		KestrelThriftClient toRet = this.clients.get(currentIndex);
		;
		currentIndex++;
		if (currentIndex == this.clients.size())
			currentIndex = 0;
		return toRet;
	}

	@Override
	public void setup() {
		try{
			logger.debug("Opening kestrel client");
			this.clients = new ArrayList<KestrelThriftClient>();
			KestrelParsedURI hostQueue = KestrelUtils.parseKestrelURI(host);
			List<KestrelServerSpec> specs = hostQueue.hosts;
			for (KestrelServerSpec spec : specs) {
				this.clients.add(new KestrelThriftClient(spec.host, spec.port));
			}
			this.queue = hostQueue.queue;
			this.cache = new ArrayList<ByteBuffer>();
			this.cacheSizeLimit = 1000;
			if(hostQueue.params.containsKey("writecache")){
				this.cacheSizeLimit = Integer.parseInt(hostQueue.params.get("writecache").get(0));
			}
			
			if(hostQueue.params.containsKey("predelete")){
				KestrelUtils.deleteQueues(this.host);
			}
			
		}catch (Exception e){
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void cleanup() {
		flushTripleCache();
		for (KestrelThriftClient client : this.clients) {
			client.close();

		}
	}


	@Override
	public void perform(byte[] item) {
		this.cache.add(ByteBuffer.wrap(item));
		if (this.cache.size() >= this.cacheSizeLimit) {
//			logger.debug(String.format("Writing item, cache size: %d/%d",this.cache.size(),this.cacheSizeLimit));
			flushTripleCache();
		}
	}

}
