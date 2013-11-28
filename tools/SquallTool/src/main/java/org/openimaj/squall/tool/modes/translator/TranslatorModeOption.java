package org.openimaj.squall.tool.modes.translator;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum TranslatorModeOption  implements CmdLineOptionsProvider{
	/**
	 * 
	 */
	JENA {
		@Override
		public TranslatorMode getOptions() {
			return new JenaTranslatorMode();
		}
	}, 
	/**
	 * 
	 */
	RIF {
		@Override
		public TranslatorMode getOptions() {
			return new RIFTranslatorMode();
		}
	};

	@Override
	public abstract TranslatorMode getOptions() ;
}
