package org.openimaj.squall.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MD5Utils {

	public static String md5Hex(String string) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
		md.update(string.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(Integer.toHexString((int) (b & 0xff)));
		}
		return sb.toString();
	}
	
}
