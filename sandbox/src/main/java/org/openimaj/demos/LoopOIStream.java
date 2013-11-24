package org.openimaj.demos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openimaj.util.data.JoinStream;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.PassThroughFunction;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.StreamLoopGuard;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LoopOIStream {
	public static void main(String[] args) {
		String[] items = new String[]{"bees","flees","cheese","knees"};
		
		Stream<String> strm = new CollectionStream<>(Arrays.asList(items));
		strm = new SplitStream<String>(strm);
		
		MultiFunction<String, String> passOnOnce = new MultiFunction<String, String>() {
			HashSet<String> seen = new HashSet<String>();
			@Override
			public List<String> apply(String in) {
				if(!seen.contains(in)){
					seen.add(in);
					return Arrays.asList(in);
				}
				return Arrays.asList();
			}
		};
		
		MultiFunction<String, String> passOnTwice = new MultiFunction<String, String>() {
			HashMap<String,Integer> seen = new HashMap<String,Integer>();
			@Override
			public List<String> apply(String in) {
				if(!seen.containsKey(in)){
					seen.put(in,1);
					return Arrays.asList(in);
				}
				if(seen.get(in) < 2){
					seen.put(in, seen.get(in)+1);
					return Arrays.asList(in);
				}
				return Arrays.asList();
			}
		};
		
		JoinStream<String> loopStrmJoin = new JoinStream<String>();
		SplitStream<String> loopStrmSplit = new SplitStream<String>(loopStrmJoin);
		
		JoinStream<String> join1 = new JoinStream<>(
			strm.map(new PassThroughFunction<String>()),
			loopStrmSplit.map(new PassThroughFunction<String>())
		);
		SplitStream<String> split1 = new SplitStream<String>(join1.map(passOnTwice));
		loopStrmJoin.addStream(
			new StreamLoopGuard<String>(split1.map(new PassThroughFunction<String>()))
		);
		
		JoinStream<String> join2 = new JoinStream<>(
			strm.map(new PassThroughFunction<String>()),
			loopStrmSplit.map(new PassThroughFunction<String>())
		);
		SplitStream<String> split2 = new SplitStream<String>(join2.map(passOnOnce));
		loopStrmJoin.addStream(
			new StreamLoopGuard<String>(split1.map(new PassThroughFunction<String>()))
		);
		
		
		new JoinStream<String>(
			split1.map(new PassThroughFunction<String>())
			,split2.map(new PassThroughFunction<String>())
		)
		.forEach(new Operation<String>() {
			
			@Override
			public void perform(String object) {
				System.out.println(object);
			}
		});
	}
}
