package org.openimaj.demos.sandbox.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.storm.spout.SimpleSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.MessageId;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class StormMessageIDCheck {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TopologyBuilder builder = new TopologyBuilder();        
		builder.setSpout("source", new SourceSpout(), 1);        
		builder.setBolt("first", new FirstBolt(), 1)
		        .shuffleGrouping("source");
		builder.setBolt("middle1", new MiddleBolt(), 1)
        		.shuffleGrouping("first","1");
		builder.setBolt("middle2", new MiddleBolt(), 1)
				.shuffleGrouping("first","2");
		builder.setBolt("final", new FinalBolt(), 1)
				.shuffleGrouping("middle1")
				.shuffleGrouping("middle2");
		
		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(2);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, builder.createTopology());
		Utils.sleep(10000);
		cluster.killTopology("test");
		cluster.shutdown();
	}

}

class SourceSpout extends SimpleSpout {

	int count = 0;
	
	@Override
	public void nextTuple() {
		if (count++ < 10)
			this.collector.emit(new Values());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}
	
}

class FirstBolt extends BaseRichBolt {

	Map stormConf;
	TopologyContext context;
	OutputCollector collector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.stormConf = stormConf;
		this.context = context;
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		this.collector.ack(input);
		this.collector.emit("1", input, new Values());
		this.collector.emit("2", input, new Values());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("1",new Fields());
		declarer.declareStream("2",new Fields());
	}
	
}

class MiddleBolt extends BaseRichBolt {

	Map stormConf;
	TopologyContext context;
	OutputCollector collector;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.stormConf = stormConf;
		this.context = context;
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		this.collector.ack(input);
		this.collector.emit(input, new Values());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}
	
}

class FinalBolt extends BaseRichBolt {

	Map stormConf;
	TopologyContext context;
	OutputCollector collector;
	
	List<MessageId> left;
	List<MessageId> right;
	
	String leftBolt;
	String rightBolt;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.stormConf = stormConf;
		this.context = context;
		this.collector = collector;
		
		this.left = new ArrayList<MessageId>();
		this.right = new ArrayList<MessageId>();
		
		leftBolt = null;
		rightBolt = null;
	}

	@Override
	public void execute(Tuple input) {
		String source = input.getSourceComponent();
		MessageId messageID = input.getMessageId();
		if (leftBolt == null)
			leftBolt = source;
		else if (leftBolt.equals(source)){
			if (right.contains(messageID))
				right.remove(messageID);
			else
				left.add(messageID);
		} else if (rightBolt == null)
			rightBolt = source;
		else if (rightBolt.equals(source)){
			if (left.contains(messageID))
				left.remove(messageID);
			else
				right.add(messageID);
		} else System.out.println("Should not reach here");
		System.out.println("\nLeft: "+left.size()+"\nRight: "+right.size());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}
	
}