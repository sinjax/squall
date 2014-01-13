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
package org.openimaj.squall.compile.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
	public static List<AnonimisedRuleVariableHolder> sortVariableHolders(Collection<AnonimisedRuleVariableHolder> t) {
		List<AnonimisedRuleVariableHolder> template = Arrays.asList(t.toArray(new AnonimisedRuleVariableHolder[t.size()]));
		
		Collections.sort(template,new Comparator<AnonimisedRuleVariableHolder>(){
			@Override
			public int compare(AnonimisedRuleVariableHolder o1,
					AnonimisedRuleVariableHolder o2) {
				return o1.identifier().compareTo(o2.identifier());
			}
		});
		
		return template;
	}
	
	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static List<String> extractOrderedFields(Collection<AnonimisedRuleVariableHolder> fieldsTemplate) {
		List<AnonimisedRuleVariableHolder> sortedFT = sortVariableHolders(fieldsTemplate);
		
		ArrayList<String> fields = new ArrayList<String>();
		for (AnonimisedRuleVariableHolder vh : sortedFT){
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
	 * @param arvh
	 * 		Variable holder to be populated with base variables and a rule-to-base variable mapping. 
	 */
	public static void extractSaneRuleAndAnonVarsAndMapping(final AnonimisedRuleVariableHolder arvh) {
		List<String> rvars = extractOrderedFields(arvh.contributors());
		for (int i = 0; i < rvars.size(); i++){
			String baseVar = "?"+i;
			arvh.addVariable(baseVar);
			arvh.putRuleToBaseVarMapEntry(rvars.get(i), baseVar);
		}
	}
	
	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static List<String> extractJoinFields(Collection<AnonimisedRuleVariableHolder> fieldsTemplate) {
		List<AnonimisedRuleVariableHolder> sortedFT = sortVariableHolders(fieldsTemplate);
		
		ArrayList<String> fields = new ArrayList<String>();
		List<String> seen = new ArrayList<String>();
		for (AnonimisedRuleVariableHolder vh : sortedFT){
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
