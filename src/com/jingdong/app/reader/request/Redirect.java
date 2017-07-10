package com.jingdong.app.reader.request;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.jingdong.app.reader.util.MZLog;

public class Redirect {
	public final static String LOCATION = "Location";
	private String location;

	private Redirect() {

	}

	public static Redirect fromJson(String jsonString) {
		JSONObject jsonObject;
		Redirect redirect = new Redirect();
		try {
			jsonObject = new JSONObject(jsonString);
			redirect.location = jsonObject.getString(LOCATION);
		} catch (JSONException e) {
			MZLog.e("Redirect", Log.getStackTraceString(e));
			redirect = null;
		}
		return redirect;

	}

	public static String toJson(String normalString) {
		JSONObject jsonObject=new JSONObject();
		try {
			jsonObject.put(LOCATION, normalString);
		} catch (JSONException e) {
			MZLog.e("Redirect", Log.getStackTraceString(e));
		}
		return jsonObject.toString();
	}

	public String getLocation() {
		return location;
	}
}
