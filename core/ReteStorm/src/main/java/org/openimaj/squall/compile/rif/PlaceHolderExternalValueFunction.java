package org.openimaj.squall.compile.rif;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rif.conditions.data.RIFDatum;
import org.openimaj.rif.conditions.data.RIFList;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class PlaceHolderExternalValueFunction implements IVFunction<Context, Context> {

	private Node[] variables;

	/**
	 * @param val
	 */
	public PlaceHolderExternalValueFunction(RIFExternalValue val) {
		this.variables = new Node[0];
		List<Node> vars = new ArrayList<Node>();
		for (int i = 0; i < val.getVal().getArgsSize(); i++){
			if (val.getVal().getArg(i) instanceof RIFList){
				RIFList list = (RIFList) val.getVal().getArg(i);
				for (int j = 0; j < list.size(); j ++){
					if (val.getVal().getArg(i) instanceof RIFDatum){
						RIFDatum datum = (RIFDatum)val.getVal().getArg(i);
						if (datum.getNode().isVariable())
							vars.add(datum.getNode());
					}
				}
			} else if (val.getVal().getArg(i) instanceof RIFDatum){
				RIFDatum datum = (RIFDatum)val.getVal().getArg(i);
				if (datum.getNode().isVariable())
					vars.add(datum.getNode());
			}
		}
		this.variables = vars.toArray(this.variables);
	}

	@Override
	public List<Context> apply(Context in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> variables() {
		List<String> ret = new ArrayList<String>();
		for (Node n : this.variables){
			ret.add(n.getName());
		}
		return ret;
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String anonimised() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
