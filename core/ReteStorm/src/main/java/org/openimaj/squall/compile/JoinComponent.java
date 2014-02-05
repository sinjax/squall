package org.openimaj.squall.compile;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.util.data.Context;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public abstract class JoinComponent<T>{
	/**
	 * @return is this component a {@link RuleWrapped} {@link IFunction}
	 */
	public abstract boolean isFunction();
	/**
	 * @return is this component a {@link CompiledProductionSystem}
	 */
	public abstract boolean isCPS();
	
	/**
	 * @return returns the T
	 */
	public abstract T getComponent();
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class CPSJoinComponent extends JoinComponent<CompiledProductionSystem> {

		private CompiledProductionSystem cps;
		
		/**
		 * @param cps
		 */
		public CPSJoinComponent(CompiledProductionSystem cps) {
			this.cps = cps;
		}

		@Override
		public boolean isFunction() {
			return false;
		}

		@Override
		public boolean isCPS() {
			return true;
		}

		@Override
		public CompiledProductionSystem getComponent() {
			return cps;
		}
		
	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class RuleWrappedFunctionJoinComponent extends JoinComponent<RuleWrappedFunction<? extends IFunction<Context,Context>>> {

		private RuleWrappedFunction<? extends IFunction<Context, Context>> cps;
		
		/**
		 * @param cps
		 */
		public RuleWrappedFunctionJoinComponent(RuleWrappedFunction<? extends IFunction<Context, Context>> cps) {
			this.cps = cps;
		}

		@Override
		public boolean isFunction() {
			return true;
		}

		@Override
		public boolean isCPS() {
			return false;
		}

		@Override
		public RuleWrappedFunction<? extends IFunction<Context, Context>> getComponent() {
			return this.cps;
		}

		
		
	}

	/**
	 * @return a typed version of {@link #getComponent()}
	 */
	@SuppressWarnings("unchecked")
	public <Q> Q getTypedComponent() {
		return (Q) this.getComponent();
	}
	
}