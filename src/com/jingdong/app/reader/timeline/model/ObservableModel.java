package com.jingdong.app.reader.timeline.model;

import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.jingdong.app.reader.util.MZLog;

public abstract class ObservableModel extends Observable {

	public final static int FAIL_INT = 0;
	public final static int SUCCESS_INT = 1;
	public final static String SUCCESS = "success";
	public final static String CODE = "code";


	/**
	 * 通知所有观察者，数据发生了变化
	 * 
	 * @param type
	 *            期待数据发生何种类型的变化。
	 * @param success
	 *            是否发生变化，取值为TRUE和FALSE中的任意一个。
	 */
	public void notifyDataChanged(int type, boolean success) {
		notifyDataChanged(type, success, false);
	}

	/**
	 * 通知所有观察者，数据发生了变化
	 * 
	 * @param type
	 *            期待数据发生何种类型的变化。
	 * @param connected
	 *            true表示网络连接存在，false表示网络连接不存在。
	 * @param hasContent
	 *            true表示有内容，false表示无内容。
	 */
	public void notifyDataChanged(int type, boolean connected, boolean hasContent) {
		notifyDataChanged(type, connected, hasContent, null);
	}

	public void notifyDataChanged(int type, boolean connected, boolean hasContent, Object object) {
		notifyDataChanged(type, connected, hasContent, object, null);
	}

	public void notifyDataChanged(int type, boolean connected, boolean hasContent, Object object, Bundle bundle) {
		Message message = Message.obtain();
		message.what = type;
		message.arg1 = connected ? SUCCESS_INT : FAIL_INT;
		message.arg2 = hasContent ? SUCCESS_INT : FAIL_INT;
		message.obj = object;
		message.setData(bundle);
		setChanged();
		notifyObservers(message);
	}

	/**
	 * 判断所发出的post请求是否正确的被服务器响应。
	 * 
	 * @param result
	 *            发出post请求后的返回值
	 * @return true表示该操作成功，false表示该操作失败
	 */
	public static boolean parsePostResult(String result) {
		boolean value = false;
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt(CODE)==0) {
				value = true;
			}
		} catch (JSONException e) {
			MZLog.e("parsePost", Log.getStackTraceString(e));
		}
		return value;
	}

	/**
	 * 判断所发出的post请求是否正确的被服务器响应。
	 * 
	 * @param result
	 *            发出post请求后的返回值。
	 * @param key
	 *            这个key的value表示post请求是否成功。
	 * @param value
	 *            post请求如果成功，需要返回的value。
	 * @return true表示该操作成功，false表示该操作失败。
	 */
	public static boolean parsePostResult(String result, String key, String value) {
		boolean success = false;
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.optString("code").equals("0")) {
				success = true;
			}
		} catch (JSONException e) {
			MZLog.e("parsePost", Log.getStackTraceString(e));
		}
		return success;
	}
}
