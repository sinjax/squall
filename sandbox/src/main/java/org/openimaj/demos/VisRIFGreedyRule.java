package org.openimaj.demos;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.utils.RifUtils;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.compile.rif.providers.predicates.ExternalLoader;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.CombinedSourceGreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.squall.utils.OPSDisplayUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class VisRIFGreedyRule {
	static class PrintAllOperation implements IOperation<Context> {
		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void perform(Context object) {
			System.out.println(object);
		}

		@Override
		public void write(Kryo kryo, Output output) {}

		@Override
		public void read(Kryo kryo, Input input) {}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public boolean forcedUnique() {
			return false;
		}
	}
	static ISource<Stream<Context>> tripleContextStream = new ISource<Stream<Context>>() {
		
		private InputStream nTripleStream;

		@Override
		public Stream<Context> apply(Stream<Context> in) {
			return apply();
		}
		
		@Override
		public Stream<Context> apply() {
			return new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
			.map(new ContextWrapper("triple"));
		}
		
		@Override
		public void setup() { 
			nTripleStream = VisRIFGreedyRule.class.getResourceAsStream("/test.rdfs");
		}
		
		@Override
		public void cleanup() {
			try {
				this.nTripleStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.nTripleStream = null;
		}

		@Override
		public void write(Kryo kryo, Output output) {}

		@Override
		public void read(Kryo kryo, Input input) {}

		@Override
		public boolean isStateless() {
			return false;
		}

		@Override
		public boolean forcedUnique() {
			return true;
		}
	};
	
	private static List<Rule> loadRules(String stream) {
		InputStream ruleStream = VisRIFGreedyRule.class.getResourceAsStream(stream);
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		return rules;
	}
	

	
	public static void main(String[] args) {
		ExternalLoader.loadExternals();
		
		String ruleSource = "file:///Users/david.monks/squall/tools/SquallTool/lsbench/query/DavidsKestrelQuery2.rif";
		
		RIFRuleSet rules = RifUtils.readRules(ruleSource);
		
		RIFCoreRuleCompiler jrc = new RIFCoreRuleCompiler();
		CompiledProductionSystem comp = jrc.compile(rules);
		CombinedSourceGreedyOrchestrator go = new CombinedSourceGreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		OPSDisplayUtils.display(orchestrated);
	}
}
