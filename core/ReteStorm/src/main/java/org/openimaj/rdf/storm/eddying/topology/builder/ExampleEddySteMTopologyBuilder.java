package org.openimaj.rdf.storm.eddying.topology.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.eddying.EddyingBolt;
import org.openimaj.rdf.storm.eddying.eddies.StormEddyBolt;
import org.openimaj.rdf.storm.eddying.routing.ExampleStormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.MultiQueryPolicyStormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.MultiQueryPolicyStormGraphRouter.MQPEddyStubStormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.Action;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.storm.spout.SimpleSpout;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

/**
 * @author david.monks
 *
 */
public class ExampleEddySteMTopologyBuilder extends TopologyBuilder {
	
	public static final int SLEEP = 3;
	public static final int STEMSIZE = 100;
	public static final long STEMDELAY = 1000;
	public static final TimeUnit STEMUNIT = TimeUnit.MINUTES;
	
	private String[] subjects = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","28","39"},
					 predicates = {"pred0",
								   "pred1",
								   "pred2"};
	private String queries = "[ (?a, 'pred0', ?b), (?b, 'pred1', ?c), (?c, 'pred2', ?d) -> (?a, 'pred3', ?d) ]";
	
	public Config initialiseConfig(){
		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(2);
		JenaStormUtils.registerSerializers(conf);
		return conf;
	}
	
	public void build(){
		final String spoutname = "data-spout";
//		final String eddyname = "data-eddy";
		final String stemprefix = "data-stem";

		// SteMs
		Map<String,String> stemMap = new HashMap<String,String>();
		StormSteMBolt[] stems = new StormSteMBolt[predicates.length];
		for (int i = 0; i < stems.length; i++) {
			stemMap.put("? \""+predicates[i]+"\" ? .", stemprefix+i);
		}
		for (int i = 0; i < stems.length; i++) {
			String tm = String.format("(?a,  'pred%d', ?b) -> ('woot',  'woot', 'woot') .",i);
 			stems[i] = new StormSteMBolt(stemprefix+i,tm,new MultiQueryPolicyStormGraphRouter(stemMap,queries)
 ,3,STEMSIZE,STEMDELAY,STEMUNIT
 );
		}
		
		// Spout
		SimpleSpout spout = new ExampleNTriplesSpout(spoutname,subjects,predicates,new MultiQueryPolicyStormGraphRouter(stemMap,queries));
		
		// Eddy
		// StormEddyBolt eddy = new StormEddyBolt(eddyname,new /*ExampleStormGraphRouter(stemMap)*/MultiQueryPolicyStormGraphRouter(stemMap,queries));
		
		// Construct Topology
		this.setSpout(spoutname, spout, 1);
		// BoltDeclarer eddyDeclarer = this.setBolt(eddyname, eddy, 10).shuffleGrouping(spoutname,eddyname);
		BoltDeclarer[] stemDeclarers = new BoltDeclarer[predicates.length];
		for (int i = 0; i < predicates.length; i++){
			String steMName = stemprefix+i;
			stemDeclarers[i] = this.setBolt(steMName, stems[i],1).shuffleGrouping(spoutname,steMName);
			for (int j = 0; j < i; j++){
				try {
					stemDeclarers[j].globalGrouping(steMName, stemprefix+j);
					stemDeclarers[i].globalGrouping(stemprefix+j, steMName);
				} catch (NullPointerException e) {
					break;
				}
			}
			// TODO sort out what to do about field groupings.
			// eddyDeclarer.globalGrouping(steMName,eddyname);
		}
	}
	
}

class ExampleNTriplesSpout extends SimpleSpout implements EddyingBolt {

	private static final long serialVersionUID = 57751357468487569L;

	private String name;
	private String[] subjects, predicates;
	private StormGraphRouter router;
	
	public ExampleNTriplesSpout(String name,String[] subs, String[] preds, StormGraphRouter sgr){
		this.name = name;
		this.subjects = subs;
		this.predicates = preds;
		this.router = sgr;
	}

	private Random random;
	
	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
					 SpoutOutputCollector collector){
		super.open(conf, context, collector);
		this.router.setOutputCollector(this);
		random = new Random();
	}
	
	@Override
	public void nextTuple() {
		Utils.sleep(ExampleEddySteMTopologyBuilder.SLEEP);
		Triple t = new Triple(Node.createLiteral(subjects[random.nextInt(subjects.length)]),
							  Node.createLiteral(predicates[random.nextInt(predicates.length)]),
							  Node.createLiteral(subjects[random.nextInt(subjects.length)]));
		Graph graph = new GraphMem();
		graph.add(t);
		Values vals = new Values();
		for (String s : this.router.getFields()){
			switch (s.charAt(0)){
			case 'a':
			case 'A':
				vals.add(Action.check);
				break;
			case 'd':
			case 'D':
				vals.add((long)0);
				break;
			case 'g':
			case 'G':
				vals.add(graph);
				break;
			case 'i':
			case 'I':
				vals.add(true);
				break;
			case 't':
			case 'T':
				vals.add(new Date().getTime());
				break;
			default:
				vals.add(0);
			}
		}
		for (String eddy : this.router.getContinuations()){
			this.emit(eddy, null, vals);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		this.router.declareOutputFields(declarer);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void emit(Tuple anchor, Values vals) {
		this.collector.emit(vals);
	}
	
	@Override
	public void emit(String name, Tuple anchor, Values vals) {
		this.collector.emit(name,vals);
	}

	@Override
	public void ack(Tuple anchor) {
		// cannot emit back to previous bolt, this is a spout.
	}
	
}