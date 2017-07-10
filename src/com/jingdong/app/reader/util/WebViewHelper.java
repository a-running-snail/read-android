/**
 * @author ThinkinBunny yfxiawei@360buy.com
 * com.jingdong.app.reader.util
 * WebViewHelper.java
 * 下午5:30:56
 */
package com.jingdong.app.reader.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.webkit.WebView;

/**
 * @author ThinkinBunny yfxiawei@360buy.com 下午5:30:56
 * 
 */
public class WebViewHelper {
	/******
	 * enablePlatformNotifications 在api 15已废弃，在15一下需要用
	 * ********/
	public static void enablePlatformNotifications() {
		try {
			final Class<?> ownerClass = Class.forName(WebView.class.getName());
			final Method method = ownerClass.getDeclaredMethod(
					"enablePlatformNotifications", new Class[] {});
			Object[] args = new Object[] {};
			method.invoke(null, args);
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

	/******
	 * disablePlatformNotifications 在api 15已废弃，在15一下需要用
	 * ********/
	public static void disablePlatformNotifications() {
		// WebView.disablePlatformNotifications();
		try {

			final Class<?> ownerClass = Class.forName(WebView.class.getName());
			final Method method = ownerClass.getDeclaredMethod(
					"disablePlatformNotifications", new Class[] {});
			Object[] args = new Object[] {};
			method.invoke(null, args);
			//
			// Class<?> webClass = Class.forName("android.webkit.WebView");
			// Method method =
			// webClass.getMethod("disablePlatformNotifications",
			// (Class<?>[])new Class[]{} );
			//
			// method.invoke(null, new Object[0]);
			MZLog.d("webviewhelper","OnlinePayActivity.disablePlatform()");
		} catch (ClassNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

}
