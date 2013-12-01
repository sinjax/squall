package org.openimaj.squall.build.storm;

import java.io.Serializable;

import org.junit.Test;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.NGNOperation;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestOperationBolt {
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static final class PrintAllOperation implements IOperation<Context>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9434419327111404L;
		private int expected;
		private int count;
		
		/**
		 * @param expected
		 */
		public PrintAllOperation(int expected) {
			this.expected = expected;
			this.count = 0;
		}

		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
			if(this.count != this.expected){
				System.out.println("Test FAILED!");
				throw new RuntimeException(String.format("THE TEST FAILED Expected %d saw %d",this.expected,this.count));
			}
		}

		@Override
		public void perform(Context object) {
			this.count ++;
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	@Test
	public void testOpBolt() throws Exception{
		OrchestratedProductionSystem ops = new OrchestratedProductionSystem();
		NGNOperation op = new NGNOperation(ops , "testop", new PrintAllOperation(0));
		
		OperationBolt b = new OperationBolt(op);
		b.innersetup();
		b.cleanup();
	}
}
