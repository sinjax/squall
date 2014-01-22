package org.openimaj.squall.build.storm;

import java.io.Serializable;

import org.junit.Test;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.NGNOperation;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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
		 * creates a PrintAllOperation that will never realistically be satisfied.
		 */
		public PrintAllOperation() {
			this(Integer.MAX_VALUE);
		}
		
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

		@Override
		public void write(Kryo kryo, Output output) {
			output.writeInt(this.expected);
			output.writeInt(this.count);
		}

		@Override
		public void read(Kryo kryo, Input input) {
			this.expected = input.readInt();
			this.count = input.readInt();
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
