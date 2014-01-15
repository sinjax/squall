package org.openimaj.storm.utils;

import java.io.ByteArrayOutputStream;
import org.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormUtils {
	/**
	 * @param function
	 * @return the byte array
	 */
	public static <T> byte[] serialiseFunction(T obj) {
		final Kryo kryo = new Kryo();
		return serialiseFunction(kryo,obj);
	}
	
	/**
	 * @param kryo
	 * @param obj
	 * @return the byte array
	 */
	public static <T> byte[] serialiseFunction(Kryo kryo, T obj){
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final Output output = new Output(bos);
		kryo.writeClassAndObject(output, obj);
		output.flush();

		return bos.toByteArray();
	}
	
	/**
	 * @param data
	 * @return
	 */
	public static <T> T deserialiseFunction(byte[] bytes) {
		final Kryo kryo = new Kryo();
		return deserialiseFunction(kryo,bytes);
	}

	/**
	 * @param kryo
	 * @param bytes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialiseFunction(Kryo kryo, byte[] bytes) {
		Object obj;
		try {
			obj = kryo.readClassAndObject(new Input(bytes));
		} catch (final KryoException e) {
			kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
			try{
				obj = kryo.readClassAndObject(new Input(bytes));
			}catch(Throwable t){
				throw new RuntimeException(t);
			}
		}
		return (T) obj;
	}
	
	/**
	 * @param identifier
	 * @return
	 */
	public static String legalizeStormIdentifier(String identifier){
		return identifier.replaceAll("\\(","")
						 .replaceAll("\\)",",")
						 .replaceAll("_","-")
						 .replaceAll("\\?","VAR");
	}
}
