package com.fetch.data.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	/**
	 * sha1sum an input bytearray
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] sha1sum(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();
			md.update(input);

			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String byteArr2HexStr(byte[] arrB) throws Exception {
        int iLen = arrB.length;
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int i = 0; i < iLen; i++) {
            int intTmp = arrB[i];
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16).toUpperCase());
        }
        return sb.toString();
    }
	
	public static String getHash(String pageUrl) throws Exception{
		byte[] b = HashUtils.sha1sum(pageUrl.getBytes());
		String out = byteArr2HexStr(b);
		return out;
	}
	
//	public static void main(String[] args) throws Exception {
//		String input;
//		String output;
//		
//		input = "http://www.56invest.com/weiboweixin/binggouxuqiu/2014/1014/978.html";
//		output = HashUtils.getHash(input);
//		System.out.println("input: " + input + " and output is: " + output);
//	}
}
