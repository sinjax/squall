package org.openimaj.squall.tool.modes.operation;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum OperationModeOption implements CmdLineOptionsProvider{
	
	/**
	 * 
	 */
	LOG{

		@Override
		public OperationMode getOptions() {
			return new Log4jOperationMode();
		}
		
	},
	/**
	 * 
	 */
	TRIPLEFILE{

		@Override
		public OperationMode getOptions() {
			return new TripleFileOperationMode();
		}
		
	},
	/**
	 * 
	 */
	TRIPLEKESTREL{

		@Override
		public OperationMode getOptions() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	;

	@Override
	public abstract OperationMode getOptions() ;

}
