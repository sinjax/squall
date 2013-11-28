package org.openimaj.squall.tool;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.rdf.storm.tool.topology.TopologyMode;
import org.openimaj.rdf.storm.tool.topology.TopologyModeOption;
import org.openimaj.squall.tool.modes.operation.OperationMode;
import org.openimaj.squall.tool.modes.operation.OperationModeOption;
import org.openimaj.squall.tool.modes.planner.PlannerMode;
import org.openimaj.squall.tool.modes.planner.PlannerModeOption;
import org.openimaj.squall.tool.modes.translator.TranslatorMode;
import org.openimaj.squall.tool.modes.translator.TranslatorModeOption;
import org.openimaj.squall.tool.modes.translator.builder.BuilderMode;
import org.openimaj.squall.tool.modes.translator.builder.BuilderModeOption;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SquallToolOptions {
	
	/**
	 * The translator
	 */
	@Option(
			name = "--translator-mode",
			aliases = "-tm",
			required = true,
			usage = "The kind of translator to consume rules from",
			handler = ProxyOptionHandler.class)
	public TranslatorModeOption tm = null;
	public TranslatorMode tmOp = null;
	
	/**
	 * The planner
	 */
	@Option(
			name = "--planner-mode",
			aliases = "-pm",
			required = true,
			usage = "The kind of planner to construct networks with",
			handler = ProxyOptionHandler.class)
	public PlannerModeOption pm = null;
	public PlannerMode pmOp = null;
	
	/**
	 * The builder
	 */
	@Option(
			name = "--builder-mode",
			aliases = "-bm",
			required = true,
			usage = "The kind of builder to realise and run the network",
			handler = ProxyOptionHandler.class)
	public BuilderModeOption bm = null;
	public BuilderMode bmOp = null;
	
	/**
	 * The builder
	 */
	@Option(
			name = "--operation",
			aliases = "-o",
			required = false,
			usage = "How the output of the procedure should be directed",
			handler = ProxyOptionHandler.class)
	public OperationModeOption om = OperationModeOption.LOG;
	public OperationMode omOp = om.getOptions();
	private String[] args;
	
	public SquallToolOptions(String[] args) throws IOException {
		this.args = args;
		prepare();
		
	}

	private void validate() {
		
	}
	
	/**
	 * 
	 */
	public void setup(){
		this.tmOp.setup(this);
		this.pmOp.setup(this);
		this.bmOp.setup(this);
	}
	
	/**
	 * 
	 */
	public void shutdown(){
		this.tmOp.shutdown(this);
		this.pmOp.shutdown(this);
		this.bmOp.shutdown(this);
	}

	/**
	 * Parse arguments and validate
	 * 
	 * @throws IOException
	 */
	public void prepare() throws IOException {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			validate();
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar SquallStormTool.jar [options...] ");
			parser.printUsage(System.err);
			System.exit(1);
		}
	}
}
