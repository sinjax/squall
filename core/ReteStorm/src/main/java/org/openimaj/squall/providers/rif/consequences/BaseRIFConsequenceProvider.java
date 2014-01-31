package org.openimaj.squall.providers.rif.consequences;

import java.util.Map;

import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.InheritsVariables;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.compile.rif.provider.FunctionProvider;
import org.openimaj.squall.compile.rif.provider.RIFExprFunctionRegistry;
import org.openimaj.squall.functions.rif.consequences.BaseConsequenceFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 */
public abstract class BaseRIFConsequenceProvider<IN> extends FunctionProvider<IN, RIFExpr> {

	/**
	 * @param reg
	 */
	public BaseRIFConsequenceProvider(
			RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	protected static abstract class RuleWrappedConsequenceFunction<T extends BaseConsequenceFunction>
										extends RuleWrappedFunction<T>
										implements InheritsVariables {

		@Override
		public boolean areSourceVariablesSet() {
			return false;
		}

		@Override
		public String getSourceVarHolderIdent() {
			return null;
		}

		@Override
		public String getSourceVarHolderIdent(Map<String, String> varMap) {
			return null;
		}

		@Override
		public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh) {
			return false;
		}

	}

}
