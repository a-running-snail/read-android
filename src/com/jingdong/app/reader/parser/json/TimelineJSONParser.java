package com.jingdong.app.reader.parser.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.jingdong.app.reader.util.MZLog;

public class TimelineJSONParser implements JSONParser {

	@Override
	public JSONArray getJsonArrayFromString(String jsonString) {
		JSONArray jsonArray = null;
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
			jsonArray = jsonObject.getJSONArray("entities");
		} catch (JSONException e) {
			MZLog.e(this.getClass().getSimpleName(), Log.getStackTraceString(e));
		}
		return jsonArray;
	}

	@Override
	public JSONObject getJsonObjectFromArray(JSONArray jsonArray, int index) {
		JSONObject jsonObject = null;
		try {
			jsonObject = jsonArray.getJSONObject(index);
		} catch (JSONException e) {
			MZLog.e(getClass().getSimpleName(), Log.getStackTraceString(e));
		}
		return jsonObject;
	}
}
