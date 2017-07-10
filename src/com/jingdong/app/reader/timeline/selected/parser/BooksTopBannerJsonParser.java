package com.jingdong.app.reader.timeline.selected.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.jingdong.app.reader.parser.json.JSONParser;
import com.jingdong.app.reader.util.MZLog;

public class BooksTopBannerJsonParser implements JSONParser {

	@Override
	public JSONArray getJsonArrayFromString(String jsonString) {

		
		JSONArray jsonArray = null;
		
	
		try {
			JSONObject object=new JSONObject(jsonString);
			JSONObject bobject=object.getJSONObject("banner");
			jsonArray = bobject.getJSONArray("recommend_entities");
		} catch (JSONException e) {
			MZLog.e(this.getClass().getSimpleName(), Log.getStackTraceString(e));
		}
		return jsonArray;

	}

	@Override
	public JSONObject getJsonObjectFromArray(JSONArray jsonArray, int index) {

		
		JSONObject jsonObject = null;
		try {
			jsonObject = jsonArray.getJSONObject(index).getJSONObject("entity");
		} catch (JSONException e) {
			MZLog.e(getClass().getSimpleName(), Log.getStackTraceString(e));
		}
		return jsonObject;
	}

}
