package com.jingdong.app.reader.util;

import java.lang.reflect.Field;

import com.jingdong.app.reader.application.MZBookApplication;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {
	
	public static float screenWidth = 0;
	public static float screenHeight = 0;

	public static String getWidth(Context context) {
		WindowManager windowManager = (WindowManager) MZBookApplication.getContext()
				.getSystemService("window");
		Display display = windowManager.getDefaultDisplay();
		return display.getWidth() + "," + display.getHeight();
	}

	/**
	 * 获取屏幕宽度
	 * @param context
	 * @return
	 */
	public static float getWidthJust(Context context) {// 返回像素宽度
		if(screenWidth == 0){
			WindowManager windowManager = (WindowManager) MZBookApplication.getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			windowManager.getDefaultDisplay().getMetrics(displayMetrics);
			return screenWidth = displayMetrics.widthPixels;
			
		}else
			return screenWidth;
	}
	/**
	 * 获取屏幕高度
	 * @param context
	 * @return
	 */
	public static float getHeightJust(Context context) {// 返回像素高度
		if(screenHeight == 0){
			WindowManager windowManager = (WindowManager) MZBookApplication.getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			windowManager.getDefaultDisplay().getMetrics(displayMetrics);
			return screenHeight = displayMetrics.heightPixels;
		}else
			return screenHeight;
	}
	
	public static int getStatusHeight (Context comContext){

		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 48;
		try {
		    c = Class.forName("com.android.internal.R$dimen");
		    obj = c.newInstance();
		    field = c.getField("status_bar_height");
		    x = Integer.parseInt(field.get(obj).toString());
		    sbar = comContext.getResources().getDimensionPixelSize(x);
		} catch(Exception e1) {
		    e1.printStackTrace();
		}
		return sbar;
	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static float dip2pxf(float dpValue) {
		final float scale = MZBookApplication.getInstance().getResources().getDisplayMetrics().density;
		return  (dpValue * scale + 0.5f);
	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(float dpValue) {
		final float scale = MZBookApplication.getInstance().getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(double dpValue) {
		final float scale = MZBookApplication.getInstance().getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(float pxValue) {
		final float scale =  MZBookApplication.getInstance().getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static int getActionbarHeight(Context context){
	    int actionBarHeight=96;
	    TypedValue tv = new TypedValue(); 
	    if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {  
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, MZBookApplication.getContext().getResources().getDisplayMetrics());  
        } 
	    return actionBarHeight;
	}
	
	public static int dip2px(Context context, float dipValue) {
		final float scale = MZBookApplication.getContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = MZBookApplication.getContext().getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

}
