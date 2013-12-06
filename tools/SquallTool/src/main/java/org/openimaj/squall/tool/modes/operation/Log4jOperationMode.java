package org.openimaj.squall.tool.modes.operation;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Log4jOperationMode implements OperationMode {

	
	private static final class PrintAllOperation implements IOperation<Context>,Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1409401688854637882L;
		private static final Logger logger = Logger.getLogger(PrintAllOperation.class);
		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void perform(Context object) {
			String prefix = "http://www.ins.cwi.nl/sib/vocabulary/lsbench-query-";
			if(object.get("rule").toString().startsWith(prefix))
			{
				logger.info(String.format("Final output from lsbench query %s: %s", object.get("rule").toString().substring(prefix.length()), object));
			}
		}
	}
	
	@Override
	public IOperation<Context> op() {
		return new PrintAllOperation();
	}

}
