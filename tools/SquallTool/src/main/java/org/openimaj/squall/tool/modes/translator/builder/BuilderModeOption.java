package org.openimaj.squall.tool.modes.translator.builder;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum BuilderModeOption implements CmdLineOptionsProvider{
	/**
	 * 
	 */
	OI{
		@Override
		public BuilderMode getOptions() {
			return new OIBuilderMode();
		}
	},
	/**
	 * 
	 */
	STORM{
		@Override
		public BuilderMode getOptions() {
			return new StormBuilderMode();
		}
	};

	@Override
	public abstract BuilderMode getOptions() ;
}
