package org.openimaj.squall.build.storm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

import backtype.storm.generated.GlobalStreamId;
import backtype.storm.grouping.CustomStreamGrouping;
import backtype.storm.task.WorkerTopologyContext;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContextVariableGrouping implements CustomStreamGrouping {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7032811266489361619L;
	private String[] bindingVars;
	private List<Integer> tasks;

	/**
	 * @param variables
	 */
	public ContextVariableGrouping(String[] variables) {
		bindingVars = variables;
	}

	@Override
	public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
		this.tasks = targetTasks;
	}

	@Override
	public List<Integer> chooseTasks(int taskId, List<Object> values) {
		Context ctx = (Context) values.get(0);// get the Context instance
		List<Node> nodes = new ArrayList<Node>();
		Map<String,Node> bindings = ctx.getTyped("bindings");
		for (String bind : this.bindingVars) {
			nodes.add(bindings.get(bind));
		}
		int index = nodes.hashCode() % this.tasks.size();
		return Arrays.asList(this.tasks.get(index));
	}

}
