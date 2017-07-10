package com.jingdong.app.reader.util;

import android.os.Build;

/**
 * Class containing some static utility methods.
 */
public class AndroidVersion {

	/*******
	 * 4.03版本
	 * *******/
	public static boolean hasICE_CREAM_SANDWICH_MR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
	}

}
