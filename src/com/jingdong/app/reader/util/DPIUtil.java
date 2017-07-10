package com.jingdong.app.reader.util;

import android.view.Display;


public class DPIUtil {

	private static float mDensity;
	private static Display defaultDisplay;

	public static void setDensity(float density) {
		mDensity = density;
	}
	
	public static int dip2px(float dipValue) {
		return (int) (dipValue * mDensity + 0.5f);
	}

	
	public static int getWidth(){
		return defaultDisplay.getWidth();
	}
	
	public static int getHeight(){
		return defaultDisplay.getHeight();
	}

}
