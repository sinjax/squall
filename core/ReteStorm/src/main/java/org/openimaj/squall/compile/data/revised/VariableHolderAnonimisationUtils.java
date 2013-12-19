/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.squall.compile.data.revised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.rdf.storm.topology.bolt.CompilationStormRuleReteBoltHolder;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.utils.Count;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * A collection of functions to represent Jena's Rete Rule Clauses as strings that are
 * independent of the variable names they contain, while maintaining Join accuracy.
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class VariableHolderAnonimisationUtils {

	/**
	 * Sort clause entries within the clause by string value, where the string used
	 * has had the variable names replaced with the literal 'VAR'.  This means the
	 * clause is sorted repeatably and independently of variable names.
	 * @param t
	 * @return List<VariableHolder>
	 */
	public static List<VariableHolder> sortVariableHolders(Collection<VariableHolder> t) {
		List<VariableHolder> template = Arrays.asList(t.toArray(new VariableHolder[t.size()]));
		
		Collections.sort(template,new Comparator<VariableHolder>(){
			@Override
			public int compare(VariableHolder o1,
					VariableHolder o2) {
				return o1.anonimised().compareTo(o2.anonimised());
			}
		});
		
		return template;
	}
	
	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static List<String> extractOrderedFields(Collection<VariableHolder> fieldsTemplate) {
		List<VariableHolder> sortedFT = sortVariableHolders(fieldsTemplate);
		
		ArrayList<String> fields = new ArrayList<String>();
		for (VariableHolder vh : sortedFT){
			for (String var : vh.ruleVariables()){
				if (!fields.contains(var)) {
					fields.add(var);
				}
			}
		}

		fields.trimToSize();
		return fields;
	}
	
	/**
	 * @param fieldsTemplate
	 * @param ruleVars 
	 * 		List to be populated with the rule variables, ordered according to first appearance in normalised rule.  Is cleared during method.
	 * @param baseVars 
	 * 		List to be populated with anonimised variables, ordered according to first appearance in normalised rule.  Is cleared during method.
	 * @param ruleToAnonVarMap
	 * 		Map to be populated with the mapping from rule variables to anonimised variables. Is cleared during method 
	 */
	public static void extractSaneRuleAndAnonVarsAndMapping(Collection<VariableHolder> fieldsTemplate,
															final List<String> ruleVars,
															final List<String> baseVars,
															final Map<String,String> ruleToAnonVarMap) {
		ruleVars.clear();
		baseVars.clear();
		ruleToAnonVarMap.clear();
		
		ruleVars.addAll(extractOrderedFields(fieldsTemplate));
		for (int i = 0; i < ruleVars.size(); i++){
			String baseVar = "?"+i;
			baseVars.add(baseVar);
			ruleToAnonVarMap.put(ruleVars.get(i), baseVar);
		}
	}
	
	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static List<String> extractJoinFields(Collection<VariableHolder> fieldsTemplate) {
		List<VariableHolder> sortedFT = sortVariableHolders(fieldsTemplate);
		
		ArrayList<String> fields = new ArrayList<String>();
		List<String> seen = new ArrayList<String>();
		for (VariableHolder vh : sortedFT){
			for (String var : vh.ruleVariables()){
				if (!seen.contains(var))
					seen.add(var);
				else if (!fields.contains(var))
					fields.add(var);
			}
		}

		fields.trimToSize();
		return fields;
	}

}
