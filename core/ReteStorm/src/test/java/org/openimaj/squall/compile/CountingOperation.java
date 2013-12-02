package org.openimaj.squall.compile;

import java.io.Serializable;

import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.util.data.Context;

/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public final class CountingOperation implements IOperation<Context>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6987720033468611350L;
		private int expected;
		private int count;
		
		/**
		 * @param expected
		 */
		public CountingOperation(int expected) {
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
				String msg = String.format("THE TEST FAILED Expected %d saw %d",this.expected,this.count);
				System.out.println(msg);
				throw new RuntimeException(msg);
			}
			else{
				System.out.println("Success!");
			}
		}

		@Override
		public void perform(Context object) {
//			System.out.println(object);
			this.count ++;
		}
	}