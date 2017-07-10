
package com.jd.ebook.rights.rpc.drm;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

class DrmObject
{
	String pString;
	int nStrLen;
}


class BinAscii {
	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String encodeHex(byte[] data) {

		//StringBuffer buf = new StringBuffer(data.length() * 2);
		int l = data.length;

		//char[] out = new char[l << 1];
		StringBuffer out = new StringBuffer(l<<1);
		// two characters form the hex value.
		for (int i = 0; i < l; i++) {
			out.append(DIGITS[(0xF0 & data[i]) >>> 4]);
			out.append(DIGITS[0x0F & data[i]]);
		}

		return out.toString();
	}
	
	
	public static String encodeHex(String s) {

		byte[] data = s.getBytes();
		//StringBuffer buf = new StringBuffer(data.length() * 2);
		int l = data.length;

		//char[] out = new char[l << 1];
		StringBuffer out = new StringBuffer(l<<1);
		// two characters form the hex value.
		for (int i = 0; i < l; i++) {
			out.append(DIGITS[(0xF0 & data[i]) >>> 4]);
			out.append(DIGITS[0x0F & data[i]]);
		}

		return out.toString();
	}


}

public class DrmLib {
	static {
		System.loadLibrary("jni_server");
	}
	

	public native DrmObject StringEncryptAES
	(String key, String inputString, int nStrLen);
	/*
	 * Class:     com_jd_ebook_rights_rpc_drm_DrmLib
	 * Method:    StringDecryptAES
	 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Lcom/jd/ebook/rights/rpc/drm/DrmObject;
	 */
	
	public native DrmObject StringDecryptAES
	(String key, String inputString, int nStrLen);

	/*
	 * Class:     com_jd_ebook_rights_rpc_drm_DrmLib
	 * Method:    GenerateKeyAES
	 * Signature: ()Lcom/jd/ebook/rights/rpc/drm/DrmObject;
	 */

	public native DrmObject GenerateKeyAES
();

	/*
	 * Class:     com_jd_ebook_rights_rpc_drm_DrmLib
	 * Method:    StringEncryptQomolangma
	 * Signature: (Ljava/lang/String;I)Lcom/jd/ebook/rights/rpc/drm/DrmObject;
	 */
	public native DrmObject StringEncryptQomolangma
	(String inputString, int nStrLen);

	/*
	 * Class:     com_jd_ebook_rights_rpc_drm_DrmLib
	 * Method:    StringDecryptQomolangma
	 * Signature: (Ljava/lang/String;I)Lcom/jd/ebook/rights/rpc/drm/DrmObject;
	 */
	public native DrmObject StringDecryptQomolangma
	(String inputString, int nStrLen);

	/*
	 * Class:     com_jd_ebook_rights_rpc_drm_DrmLib
	 * Method:    Hash
	 * Signature: (Ljava/lang/String;ILjava/lang/String;I)V
	 */
	public native DrmObject Hash
	(String inputString, int nStrLen);


		/*
	 * Class:     com_jd_ebook_rights_rpc_drm_DrmLib
	 * Method:    GenerateRandom
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
	 */
	public native DrmObject GenerateRandom
	  ();
	
	
	private static void test_random()
	{
		DrmLib a = new DrmLib();
		for (int i = 0;  i < 1000; ++i)
		{
			DrmObject o = a.GenerateRandom();
			//System.out.println(o.pString);
			assert(44 == o.pString.length());
			System.out.println(i);
		}
	}
	
	private static String random(int n) throws NoSuchAlgorithmException
	{
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		byte bytes[] = new byte[n];
	     random.nextBytes(bytes);
	     return Base64.encode(bytes).substring(0, n);
	}
	
	private static void test_StringEncryptQomolangma() throws NoSuchAlgorithmException
	{
		DrmLib a = new DrmLib();
		for (int i = 1; i < 1000; ++i) {
			String s = DrmLib.random(i);

			DrmObject os = a.StringEncryptQomolangma(s, s.length());
			System.out.println(i);
			DrmObject op = a.StringDecryptQomolangma(os.pString, os.nStrLen);
			assert (i == op.nStrLen);
			assert (s.equals(op.pString));
		}
	}
	

	private static void test_StringEncryptAES() throws NoSuchAlgorithmException
	{
		DrmLib a = new DrmLib();
		String key = DrmLib.random(16);
		for (int i = 1; i < 1000; i += 1) {
			String s = DrmLib.random(i);

			System.out.println(i);
			//System.out.println(s);
			DrmObject os = a.StringEncryptAES(key, s, s.length());
			//System.out.println(os.nStrLen);
			//System.out.println(os.pString);
			DrmObject op = a.StringDecryptAES(key, os.pString, os.nStrLen);
			assert (i == op.nStrLen);
			assert (s.equals(op.pString));
		}
	}

	public static void main(String[] args)
	{
		 try {
			//test_StringEncryptQomolangma();
			test_StringEncryptAES();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}



/*
 * Base64.java
 *
 * Brazil project web application toolkit,
 * export version: 2.0 
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is included as
 * the file "license.terms", and also available at
 * http://www.sun.com/
 * 
 * The Original Code is from:
 *    Brazil project web application toolkit release 2.0.
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems,
 * Inc. All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  1.9
 * Created by cstevens on 00/04/17
 * Last modified by suhler on 02/07/24 10:49:48
 */

//package sunlabs.brazil.util;

/**
 * Utility to base64 encode and decode a string.
 * @author      Stephen Uhler
 * @version	1.9, 02/07/24
 */

class Base64 {
    static byte[] encodeData;
    static String charSet = 
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    
    static {
    	encodeData = new byte[64];
	for (int i = 0; i<64; i++) {
	    byte c = (byte) charSet.charAt(i);
	    encodeData[i] = c;
	}
    }

    private Base64() {}

    /**
     * base-64 encode a string
     * @param s		The ascii string to encode
     * @returns		The base64 encoded result
     */

    public static String
    encode(String s) {
        return encode(s.getBytes());
    }

    /**
     * base-64 encode a byte array
     * @param src	The byte array to encode
     * @returns		The base64 encoded result
     */

    public static String
    encode(byte[] src) {
	return encode(src, 0, src.length);
    }

    /**
     * base-64 encode a byte array
     * @param src	The byte array to encode
     * @param start	The starting index
     * @param len	The number of bytes
     * @returns		The base64 encoded result
     */

    public static String
    encode(byte[] src, int start, int length) {
        byte[] dst = new byte[(length+2)/3 * 4 + length/72];
        int x = 0;
        int dstIndex = 0;
        int state = 0;	// which char in pattern
        int old = 0;	// previous byte
        int len = 0;	// length decoded so far
	int max = length + start;
        for (int srcIndex = start; srcIndex<max; srcIndex++) {
	    x = src[srcIndex];
	    switch (++state) {
	    case 1:
	        dst[dstIndex++] = encodeData[(x>>2) & 0x3f];
		break;
	    case 2:
	        dst[dstIndex++] = encodeData[((old<<4)&0x30) 
	            | ((x>>4)&0xf)];
		break;
	    case 3:
	        dst[dstIndex++] = encodeData[((old<<2)&0x3C) 
	            | ((x>>6)&0x3)];
		dst[dstIndex++] = encodeData[x&0x3F];
		state = 0;
		break;
	    }
	    old = x;
	    if (++len >= 72) {
	    	dst[dstIndex++] = (byte) '\n';
	    	len = 0;
	    }
	}

	/*
	 * now clean up the end bytes
	 */

	switch (state) {
	case 1: dst[dstIndex++] = encodeData[(old<<4) & 0x30];
	   dst[dstIndex++] = (byte) '=';
	   dst[dstIndex++] = (byte) '=';
	   break;
	case 2: dst[dstIndex++] = encodeData[(old<<2) & 0x3c];
	   dst[dstIndex++] = (byte) '=';
	   break;
	}
	return new String(dst);
    }

    /**
     * A Base64 decoder.  This implementation is slow, and 
     * doesn't handle wrapped lines.
     * The output is undefined if there are errors in the input.
     * @param s		a Base64 encoded string
     * @returns		The byte array eith the decoded result
     */

    public static byte[]
    decode(String s) {
      int end = 0;	// end state
      if (s.endsWith("=")) {
	  end++;
      }
      if (s.endsWith("==")) {
	  end++;
      }
      int len = (s.length() + 3)/4 * 3 - end;
      byte[] result = new byte[len];
      int dst = 0;
      try {
	  for(int src = 0; src< s.length(); src++) {
	      int code =  charSet.indexOf(s.charAt(src));
	      if (code == -1) {
	          break;
	      }
	      switch (src%4) {
	      case 0:
	          result[dst] = (byte) (code<<2);
	          break;
	      case 1: 
	          result[dst++] |= (byte) ((code>>4) & 0x3);
	          result[dst] = (byte) (code<<4);
	          break;
	      case 2:
	          result[dst++] |= (byte) ((code>>2) & 0xf);
	          result[dst] = (byte) (code<<6);
	          break;
	      case 3:
	          result[dst++] |= (byte) (code & 0x3f);
	          break;
	      }
	  }
      } catch (ArrayIndexOutOfBoundsException e) {}
      return result;
    }

    /**
     * Test the decoder and encoder.
     * Call as <code>Base64 [string]</code>.
     */

    public static void
    main(String[] args) {
    	System.out.println("encode: " + args[0]  + " -> (" 
    	    + encode(args[0]) + ")");
    	System.out.println("decode: " + args[0]  + " -> (" 
    	    + new String(decode(args[0])) + ")");
    }
}