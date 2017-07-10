package com.jingdong.app.reader.util;

import com.jingdong.app.reader.config.Configuration;

import static com.jingdong.app.reader.config.Configuration.isDebugMode;

public class Log {

	// log信息比较重要，放在config.properties里容易被人更改，在lag文件里写死
	// private static boolean printLog = Configuration.sPrintLog;
	private final static boolean printClassLog = Configuration.sPrintClassLog;
	// Boolean.parseBoolean(Configuration.getProperty(Configuration.PRINT_LOG,
	// "false"));

	public final static boolean D = true;
	public final static boolean V = true;
	public final static boolean I = true;
	public final static boolean W = true;
	public final static boolean E = true;
	public final static boolean WL = Configuration.sWriteLog;

	public static void d(String tag, String msg) {
		if (isDebugMode)
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable tr) {

		if (isDebugMode)
			android.util.Log.d(tag, msg, tr);
	}

	public static void v(String tag, String msg) {
		if (isDebugMode)
			android.util.Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (isDebugMode)
			android.util.Log.v(tag, msg, tr);
	}

	public static void i(String tag, String msg) {
		if (isDebugMode)
			android.util.Log.i(tag, msg);
	}

	public static void i(String msg) {

		if (isDebugMode)
			android.util.Log.i(getStackTraceName(), msg);
	}

	public static void i(String tag, String msg, Throwable tr) {

		if (isDebugMode)
			android.util.Log.i(tag, msg, tr);
	}

	public static void w(String tag, String msg) {

		if (isDebugMode)
			android.util.Log.w(tag, msg);
	}

	public static void w(String tag, Throwable tr) {

		if (isDebugMode)
			android.util.Log.w(tag, tr);
	}

	public static void w(String tag, String msg, Throwable tr) {

		if (isDebugMode)
			android.util.Log.w(tag, msg, tr);
	}

	public static void e(String tag, String msg) {

		if (isDebugMode)
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (isDebugMode)
			android.util.Log.e(tag, msg, tr);

	}

	public static String getStackTraceName() {
		if (!printClassLog) {
			return "";
		}
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String stackTraceName = null;
		if (stackTrace.length > 4) {
			stackTraceName = stackTrace[4].getClassName()
					+ stackTrace[4].getMethodName();
		}
		return stackTraceName;
	}

}
