package com.jingdong.app.reader.util;

/**
 * 
 * @author xuhongwei
 *
 */
public class SecretKeyUtil {

	static {
		System.loadLibrary("jdsecure");
	}

	/**
	 * 加密方法
	 * @param ebookId 图书id
	 * @param userPin 用户pin
	 * @param source  需要加密的字符串
	 * @return 加密后的字符串
	 */
	public static native String encrypt(String ebookId, String userPin, String source);
	/**
	 * 解密方法
	 * @param ebookId 图书id
	 * @param userPin 用户pin
	 * @param source  加密字符串
	 * @return 解密后的字符串
	 */
	public static native String decrypt(String ebookId, String userPin, String source);

}
