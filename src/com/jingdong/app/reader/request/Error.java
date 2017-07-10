package com.jingdong.app.reader.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.util.MZLog;

import android.util.Log;

public class Error {
	public final static int NOT_UPLOAD = 12002;
	public final static int NOT_BORROWABLE = 12003;
	public final static String ERROR = "error";
	private final static String CODE = "code";
	private final static String MESSAGE = "message";
	private int code;
	private String message;

	private Error() {

	}

	public static Error fromJson(String jsonString) {
		Error error = new Error();
		try {
			JSONObject jsonObject = new JSONObject(jsonString).getJSONObject(ERROR);
			error.code = jsonObject.optInt(CODE);
			error.message = jsonObject.optString(MESSAGE);
		} catch (JSONException e) {
			MZLog.d("Error", Log.getStackTraceString(e));
			error = null;
		}
		return error;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isNotUpload() {
		return code == NOT_UPLOAD;
	}

	public boolean isNotBorrowable() {
		return code == NOT_BORROWABLE;
	}
}
