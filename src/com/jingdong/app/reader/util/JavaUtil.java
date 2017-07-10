package com.jingdong.app.reader.util;

public class JavaUtil {

	/**
	 * 
	 * @Title: isArrayEqual
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param arr1
	 * @param @param arr2
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean isArrayEqual(String[] arr1, String[] arr2) {

		if (arr1.length != arr2.length)
			return false;
		else {
			boolean flag1 = true;
			boolean flag2 = true;
			for (int i = 0; i < arr1.length; i++) {
				if (!arr1[i].equals(arr2[i]) ) {
					flag1 = false;
				}
			}
			if (!flag1) {
				for (int i = 0; i < arr1.length; i++) {
					if (!arr1[i].equals(arr2[arr1.length - 1 - i])) {
						flag2 = false;
					}
				}
			}
			return flag1 || flag2;
		}
	}
}
