package com.jingdong.app.reader.util;

import android.text.TextUtils;

public class StringUtil {

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	/**
	 * 转义正则特殊字符 （$()*+.[]?\^{},|）
	 * 
	 * @param keyword
	 * @return
	 */
	public static String escapeExprSpecialWord(String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
			String[] hexCode = { "\\u005C", "\\u0024", "\\u0028", "\\u0029", "\\u002A", "\\u002B", "\\u002E", "\\u005B", "\\u005D", "\\u003F", "\\u005E", "\\u007B", "\\u007D", "\\u007C" };
			for (int i = 0; i < fbsArr.length; i++) {
				String key = fbsArr[i];
				if (keyword.contains(key)) {
					keyword = keyword.replace(key, hexCode[i]);
				}
			}
		}
		return keyword;
	}
}
